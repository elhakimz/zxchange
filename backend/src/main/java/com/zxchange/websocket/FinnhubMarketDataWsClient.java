package com.zxchange.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxchange.model.dto.BarDto;
import com.zxchange.model.dto.QuoteDto;
import com.zxchange.service.EncryptionService;
import com.zxchange.repository.SettingsRepository;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class FinnhubMarketDataWsClient {

    private static final Logger logger = LoggerFactory.getLogger(FinnhubMarketDataWsClient.class);
    private static final int[] BACKOFF_MS = {1_000, 2_000, 4_000, 8_000, 16_000};
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final StompBroadcaster stompBroadcaster;
    private final SettingsRepository settingsRepository;
    private final com.zxchange.repository.WatchlistRepository watchlistRepository;
    private final EncryptionService encryptionService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private int reconnectAttempt = 0;

    @Value("${zxchange.finnhub.ws-url}")
    private String wsUrl;

    @Value("${FINNHUB_API_KEY:}")
    private String envApiKey;

    private WebSocket webSocket;
    private final Set<String> subscribedSymbols = ConcurrentHashMap.newKeySet();
    private volatile boolean reconnectScheduled = false;

    public FinnhubMarketDataWsClient(StompBroadcaster stompBroadcaster,
                                    SettingsRepository settingsRepository,
                                    com.zxchange.repository.WatchlistRepository watchlistRepository,
                                    EncryptionService encryptionService) {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.stompBroadcaster = stompBroadcaster;
        this.settingsRepository = settingsRepository;
        this.watchlistRepository = watchlistRepository;
        this.encryptionService = encryptionService;
    }

    @PostConstruct
    public void init() {
        watchlistRepository.findAllWithSymbols().forEach(w -> {
            w.getSymbols().forEach(s -> subscribedSymbols.add(s.getSymbol()));
        });

        if (!subscribedSymbols.isEmpty()) {
            connect();
        }
    }

    public void connect() {
        if (webSocket != null) return;

        String apiKey = getApiKey();
        if (apiKey.isEmpty()) {
            logger.warn("Finnhub API key missing, cannot connect WebSocket");
            return;
        }

        String fullWsUrl = wsUrl + "?token=" + apiKey;
        Request request = new Request.Builder().url(fullWsUrl).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                logger.info("TCP Connection established to Finnhub Market Data");
                reconnectAttempt = 0;
                resubscribeAll();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                logger.debug("RAW Finnhub Message: " + text);
                handleMessage(text);
            }

            @Override
            public void onClosing(WebSocket ws, int code, String reason) {
                logger.info("Finnhub WS Closing: {} [{}]", reason, code);
                FinnhubMarketDataWsClient.this.webSocket = null;
                scheduleReconnect();
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                logger.error("Finnhub WS Error: {}", t.getMessage());
                FinnhubMarketDataWsClient.this.webSocket = null;
                scheduleReconnect();
            }
        });
    }

    private void scheduleReconnect() {
        if (reconnectScheduled) return;
        reconnectScheduled = true;

        int delay = reconnectAttempt < BACKOFF_MS.length
            ? BACKOFF_MS[reconnectAttempt++]
            : BACKOFF_MS[BACKOFF_MS.length - 1];

        logger.info("Scheduling Finnhub WS reconnect in {} ms (attempt {})", delay, reconnectAttempt);
        scheduler.schedule(() -> {
            reconnectScheduled = false;
            if (!subscribedSymbols.isEmpty()) {
                logger.info("Attempting Finnhub WS reconnect...");
                connect();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    public void subscribe(String symbol) {
        if (symbol == null || symbol.isEmpty()) return;
        subscribedSymbols.add(symbol.toUpperCase());

        if (webSocket != null) {
            webSocket.send(String.format("{\"type\":\"subscribe\",\"symbol\":\"%s\"}", symbol.toUpperCase()));
        } else {
            connect();
        }
    }

    public void resubscribe(List<String> symbols) {
        for (String s : symbols) {
            subscribedSymbols.add(s.toUpperCase());
        }
        connect();
    }

    private void handleMessage(String text) {
        try {
            JsonNode root = objectMapper.readTree(text);
            if (root.has("type") && "trade".equals(root.get("type").asText())) {
                JsonNode data = root.get("data");
                if (data.isArray()) {
                    for (JsonNode trade : data) {
                        QuoteDto quote = new QuoteDto(
                                trade.get("s").asText(),
                                trade.get("p").asDouble(), // Finnhub 'p' is last price
                                0, // volume
                                null,
                                trade.get("p").asDouble(),
                                0,
                                null,
                                java.time.Instant.ofEpochMilli(trade.get("t").asLong()).toString()
                        );
                        stompBroadcaster.broadcastQuote(quote);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error handling Finnhub WS message: {}", e.getMessage());
        }
    }

    private void resubscribeAll() {
        if (subscribedSymbols.isEmpty() || webSocket == null) {
            return;
        }

        for (String symbol : subscribedSymbols) {
            webSocket.send(String.format("{\"type\":\"subscribe\",\"symbol\":\"%s\"}", symbol));
        }
        logger.info("Resubscribed to {} symbols", subscribedSymbols.size());
    }

    private String getApiKey() {
        return (envApiKey != null && !envApiKey.isEmpty()) ? envApiKey : getSetting("FINNHUB_API_KEY");
    }

    private String getSetting(String key) {
        return settingsRepository.findByKey(key)
                .map(s -> s.isEncrypted() ? encryptionService.decrypt(s.getValue()) : s.getValue())
                .orElse("");
    }
}
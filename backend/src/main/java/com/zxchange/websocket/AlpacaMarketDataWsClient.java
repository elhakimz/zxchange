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
public class AlpacaMarketDataWsClient {

    private static final Logger logger = LoggerFactory.getLogger(AlpacaMarketDataWsClient.class);
    private static final int[] BACKOFF_MS = {1_000, 2_000, 4_000, 8_000, 16_000};
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final StompBroadcaster stompBroadcaster;
    private final SettingsRepository settingsRepository;
    private final com.zxchange.repository.WatchlistRepository watchlistRepository;
    private final EncryptionService encryptionService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private int reconnectAttempt = 0;

    @Value("${zxchange.alpaca.data-ws-url}")
    private String wsUrl;

    @Value("${ALPACA_API_KEY:}")
    private String envApiKey;

    @Value("${ALPACA_API_SECRET:}")
    private String envApiSecret;

    private WebSocket webSocket;
    private final Set<String> subscribedSymbols = ConcurrentHashMap.newKeySet();
    private volatile boolean reconnectScheduled = false;

    public AlpacaMarketDataWsClient(StompBroadcaster stompBroadcaster,
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

        Request request = new Request.Builder().url(wsUrl).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                logger.info("TCP Connection established to Alpaca Market Data");
                reconnectAttempt = 0;
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                logger.debug("RAW Alpaca Message: " + text);
                handleMessage(text);
            }

            @Override
            public void onClosing(WebSocket ws, int code, String reason) {
                logger.info("Alpaca WS Closing: {} [{}]", reason, code);
                AlpacaMarketDataWsClient.this.webSocket = null;
                scheduleReconnect();
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                logger.error("Alpaca WS Error: {}", t.getMessage());
                AlpacaMarketDataWsClient.this.webSocket = null;
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

        logger.info("Scheduling Alpaca WS reconnect in {} ms (attempt {})", delay, reconnectAttempt);
        scheduler.schedule(() -> {
            reconnectScheduled = false;
            if (!subscribedSymbols.isEmpty()) {
                logger.info("Attempting Alpaca WS reconnect...");
                connect();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void authenticate() {
        String apiKey = (envApiKey != null && !envApiKey.isEmpty()) ? envApiKey : getSetting("ALPACA_API_KEY");
        String apiSecret = (envApiSecret != null && !envApiSecret.isEmpty()) ? envApiSecret : getSetting("ALPACA_API_SECRET");

        if (apiKey.isEmpty() || apiSecret.isEmpty()) {
            logger.warn("Missing Alpaca API keys, cannot authenticate WebSocket");
            return;
        }

        webSocket.send(String.format("{\"action\": \"auth\", \"key\": \"%s\", \"secret\": \"%s\"}", apiKey, apiSecret));
        logger.info("Sending authentication message to Alpaca...");
    }

    public void subscribe(String symbol) {
        if (symbol == null || symbol.isEmpty()) return;
        subscribedSymbols.add(symbol.toUpperCase());

        if (webSocket != null) {
            webSocket.send(String.format(
                "{\"action\": \"subscribe\", \"quotes\": [\"%s\"], \"bars\": [\"%s\"]}",
                symbol.toUpperCase(), symbol.toUpperCase()));
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
            if (root.isArray()) {
                for (JsonNode node : root) {
                    JsonNode typeNode = node.get("T");
                    if (typeNode == null) continue;

                    String msgType = typeNode.asText();
                    if ("q".equals(msgType)) {
                        QuoteDto quote = new QuoteDto(
                                node.get("S").asText(),
                                node.get("bp").asDouble(),
                                node.get("bs").asInt(),
                                null,
                                node.get("ap").asDouble(),
                                node.get("as").asInt(),
                                null,
                                node.get("t").asText()
                        );
                        stompBroadcaster.broadcastQuote(quote);
                    } else if ("b".equals(msgType)) {
                        BarDto bar = new BarDto(
                                node.get("S").asText(),
                                node.get("t").asText(),
                                node.get("o").asDouble(),
                                node.get("h").asDouble(),
                                node.get("l").asDouble(),
                                node.get("c").asDouble(),
                                node.get("v").asLong(),
                                node.get("vw").asDouble(),
                                node.get("n").asInt()
                        );
                        stompBroadcaster.broadcastBar(bar, "1Min");
                        logger.debug("Real- time bar for {}: {} c={}", bar. symbol(), bar. timestamp(), bar.close());
                    } else if ("success".equals(msgType)) {
                        String msg = node.get("msg").asText();
                        if ("connected".equals(msg)) {
                            logger.info("Alpaca WS: Connected. Authenticating...");
                            authenticate();
                        } else if ("authenticated".equals(msg)) {
                            logger.info("Alpaca WS: Authenticated. Resubscribing {} symbols...", subscribedSymbols.size());
                            resubscribeAll();
                        }
                    } else if ("subscription".equals(msgType)) {
                        logger.info("Alpaca WS: Subscription confirmed");
                    } else if ("error".equals(msgType)) {
                        logger.error("Alpaca WS Error: {}", node.toString());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error handling Alpaca WS message: {}", e.getMessage());
        }
    }

    private void resubscribeAll() {
        if (subscribedSymbols.isEmpty()) {
            logger.info("No symbols to subscribe to.");
            return;
        }

        StringBuilder quotesPart = new StringBuilder();
        StringBuilder barsPart = new StringBuilder();
        int i = 0;
        for (String symbol : subscribedSymbols) {
            quotesPart.append("\"").append(symbol).append("\"");
            barsPart.append("\"").append(symbol).append("\"");
            if (++i < subscribedSymbols.size()) {
                quotesPart.append(",");
                barsPart.append(",");
            }
        }
        String subMsg = String.format("{\"action\": \"subscribe\", \"quotes\": [%s], \"bars\": [%s]}",
                quotesPart.toString(), barsPart.toString());

        webSocket.send(subMsg);
        logger.info("Resubscribing all symbols: {}", subMsg);
    }

    private String getSetting(String key) {
        return settingsRepository.findByKey(key)
                .map(s -> s.isEncrypted() ? encryptionService.decrypt(s.getValue()) : s.getValue())
                .orElse("");
    }
}
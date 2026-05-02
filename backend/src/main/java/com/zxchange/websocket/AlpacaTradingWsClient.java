package com.zxchange.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxchange.service.EncryptionService;
import com.zxchange.model.dto.OrderResponseDto;
import com.zxchange.repository.SettingsRepository;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class AlpacaTradingWsClient {

    private static final Logger logger = LoggerFactory.getLogger(AlpacaTradingWsClient.class);
    private static final int[] BACKOFF_MS = {1_000, 2_000, 4_000, 8_000, 16_000};
    private static final String TRADING_WS_URL = "wss://stream.data.alpaca.markets/v2/paper";

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final StompBroadcaster stompBroadcaster;
    private final SettingsRepository settingsRepository;
    private final EncryptionService encryptionService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private int reconnectAttempt = 0;

    @Value("${ALPACA_API_KEY:}")
    private String envApiKey;

    @Value("${ALPACA_API_SECRET:}")
    private String envApiSecret;

    private WebSocket webSocket;
    private volatile boolean reconnectScheduled = false;
    private volatile boolean authenticated = false;

    public AlpacaTradingWsClient(StompBroadcaster stompBroadcaster,
                                SettingsRepository settingsRepository,
                                EncryptionService encryptionService) {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.stompBroadcaster = stompBroadcaster;
        this.settingsRepository = settingsRepository;
        this.encryptionService = encryptionService;
    }

    public void connect() {
        if (webSocket != null) return;

        Request request = new Request.Builder().url(TRADING_WS_URL).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                logger.info("Alpaca Trading WS: TCP Connection established");
                reconnectAttempt = 0;
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                logger.debug("Alpaca Trading WS Message: {}", text);
                handleMessage(text);
            }

            @Override
            public void onClosing(WebSocket ws, int code, String reason) {
                logger.info("Alpaca Trading WS Closing: {} [{}]", reason, code);
                authenticated = false;
                AlpacaTradingWsClient.this.webSocket = null;
                scheduleReconnect();
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                logger.error("Alpaca Trading WS Error: {}", t.getMessage());
                authenticated = false;
                AlpacaTradingWsClient.this.webSocket = null;
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

        logger.info("Scheduling Trading WS reconnect in {} ms (attempt {})", delay, reconnectAttempt);
        scheduler.schedule(() -> {
            reconnectScheduled = false;
            logger.info("Attempting Trading WS reconnect...");
            connect();
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void authenticate() {
        String apiKey = (envApiKey != null && !envApiKey.isEmpty()) ? envApiKey : getSetting("ALPACA_API_KEY");
        String apiSecret = (envApiSecret != null && !envApiSecret.isEmpty()) ? envApiSecret : getSetting("ALPACA_API_SECRET");

        if (apiKey.isEmpty() || apiSecret.isEmpty()) {
            logger.warn("Missing Alpaca API keys, cannot authenticate Trading WebSocket");
            return;
        }

        String authMsg = String.format(
            "{\"action\": \"auth\", \"key\": \"%s\", \"secret\": \"%s\"}",
            apiKey, apiSecret
        );
        webSocket.send(authMsg);
        logger.info("Alpaca Trading WS: Authenticating...");
    }

    private void subscribe() {
        if (webSocket == null || !authenticated) return;

        String subMsg = "{\"action\": \"subscribe\", \"trades\": [\"*\"], \"quotes\": [\"*\"], \"account\": [\"*\"], \"orders\": [\"*\"]}";
        webSocket.send(subMsg);
        logger.info("Alpaca Trading WS: Subscribed to trade_updates, quotes, account, orders");
    }

    private void handleMessage(String text) {
        try {
            JsonNode root = objectMapper.readTree(text);
            if (root.isArray()) {
                for (JsonNode node : root) {
                    JsonNode typeNode = node.get("T");
                    if (typeNode == null) continue;

                    String msgType = typeNode.asText();
                    if ("success".equals(msgType)) {
                        String msg = node.get("msg").asText();
                        if ("authenticated".equals(msg)) {
                            logger.info("Alpaca Trading WS: Authenticated successfully");
                            authenticated = true;
                            subscribe();
                        }
                    } else if ("error".equals(msgType)) {
                        logger.error("Alpaca Trading WS Error: {}", node.toString());
                    } else if ("orders".equals(msgType)) {
                        handleOrderUpdate(node);
                    } else if ("account".equals(msgType)) {
                        handleAccountUpdate(node);
                    } else if ("trade".equals(msgType)) {
                        handleTradeUpdate(node);
                    } else if ("quote".equals(msgType)) {
                        handleQuoteUpdate(node);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error handling Trading WS message: {}", e.getMessage());
        }
    }

    private void handleOrderUpdate(JsonNode node) {
        try {
            OrderResponseDto order = new OrderResponseDto();
            order.setId(node.get("id").asText());
            order.setClientOrderId(node.has("c") ? node.get("c").asText() : null);
            order.setSymbol(node.get("symbol").asText());
            order.setSide(node.get("side").asText());
            order.setType(node.get("type").asText());
            order.setTimeInForce(node.get("t").asText());
            order.setStatus(node.get("status").asText());
            order.setSubmittedAt(node.has("submitted_at") ? node.get("submitted_at").asText() : null);
            order.setFilledAt(node.has("filled_at") ? node.get("filled_at").asText() : null);
            order.setExpiredAt(node.has("expired_at") ? node.get("expired_at").asText() : null);
            order.setCanceledAt(node.has("canceled_at") ? node.get("canceled_at").asText() : null);
            order.setFilledQty(node.has("filled_qty") ? node.get("filled_qty").asInt() : 0);
            order.setFilledAvgPrice(node.has("filled_avg_price") ? node.get("filled_avg_price").asDouble() : 0.0);
            order.setLimitPrice(node.has("limit_price") ? node.get("limit_price").asDouble() : 0.0);
            order.setStopPrice(node.has("stop_price") ? node.get("stop_price").asDouble() : 0.0);

            stompBroadcaster.broadcastOrderUpdate(order);
            logger.info("Order update: {} status={}", order.getId(), order.getStatus());
        } catch (Exception e) {
            logger.error("Error parsing order update: {}", e.getMessage());
        }
    }

    private void handleAccountUpdate(JsonNode node) {
        stompBroadcaster.broadcastAccountUpdate(node.toString());
        logger.debug("Account update received");
    }

    private void handleTradeUpdate(JsonNode node) {
        stompBroadcaster.broadcastTradeUpdate(node.toString());
        logger.debug("Trade update: {}", node.get("S"));
    }

    private void handleQuoteUpdate(JsonNode node) {
        stompBroadcaster.broadcastQuoteUpdate(node.toString());
        logger.debug("Quote update: {}", node.get("S"));
    }

    private String getSetting(String key) {
        return settingsRepository.findByKey(key)
                .map(s -> s.isEncrypted() ? encryptionService.decrypt(s.getValue()) : s.getValue())
                .orElse("");
    }
}
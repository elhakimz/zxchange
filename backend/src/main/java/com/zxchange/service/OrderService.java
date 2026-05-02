package com.zxchange.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxchange.model.dto.OrderRequestDto;
import com.zxchange.model.dto.OrderResponseDto;
import com.zxchange.repository.SettingsRepository;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SettingsRepository settingsRepository;
    private final EncryptionService encryptionService;

    @Value("${zxchange.alpaca.base-url}")
    private String baseUrl;

    @Value("${zxchange.alpaca.paper-base-url}")
    private String paperBaseUrl;

    @Value("${ALPACA_API_KEY:}")
    private String envApiKey;

    @Value("${ALPACA_API_SECRET:}")
    private String envApiSecret;

    public OrderService(SettingsRepository settingsRepository, EncryptionService encryptionService) {
        this.settingsRepository = settingsRepository;
        this.encryptionService = encryptionService;
    }

    private String getApiKey() {
        return (envApiKey != null && !envApiKey.isEmpty()) ? envApiKey : getSetting("ALPACA_API_KEY");
    }

    private String getApiSecret() {
        return (envApiSecret != null && !envApiSecret.isEmpty()) ? envApiSecret : getSetting("ALPACA_API_SECRET");
    }

    private String getBaseUrl() {
        String endpoint = getSetting("ALPACA_ENDPOINT");
        if (endpoint != null && !endpoint.isEmpty()) {
            return endpoint.replace("/v2", "");
        }
        boolean isPaper = "true".equalsIgnoreCase(getSetting("ALPACA_IS_PAPER"));
        return isPaper ? paperBaseUrl : baseUrl;
    }

    private String getSetting(String key) {
        return settingsRepository.findByKey(key)
                .map(s -> s.isEncrypted() ? encryptionService.decrypt(s.getValue()) : s.getValue())
                .orElse("");
    }

    public OrderResponseDto placeOrder(OrderRequestDto request) throws IOException {
        String apiKey = getApiKey();
        String apiSecret = getApiSecret();
        
        if (apiKey.isEmpty() || apiSecret.isEmpty()) {
            throw new IOException("Missing API keys");
        }
        
        String url = getBaseUrl() + "/v2/orders";

        String requestBody = buildOrderJson(request);
        logger.info("Placing order: {} to {} with key={}", requestBody, url, apiKey);

        RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/json"));
        Request httpRequest = new Request.Builder()
                .url(url)
                .addHeader("APCA-API-KEY-ID", getApiKey())
                .addHeader("APCA-API-SECRET-KEY", getApiSecret())
                .post(body)
                .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                logger.error("Order placement failed: {} - {}", response.code(), errorBody);
                throw new IOException("Order failed: " + response.code() + " - " + errorBody);
            }
            String responseBody = response.body().string();
            logger.info("Order placed successfully: {}", responseBody);
            return objectMapper.readValue(responseBody, OrderResponseDto.class);
        }
    }

    public List<OrderResponseDto> getOpenOrders() throws IOException {
        String url = getBaseUrl() + "/v2/orders?status=open";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("APCA-API-KEY-ID", getApiKey())
                .addHeader("APCA-API-SECRET-KEY", getApiSecret())
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Failed to get orders: {}", response.code());
                return new ArrayList<>();
            }
            String responseBody = response.body().string();
            List<OrderResponseDto> orders = objectMapper.readValue(responseBody,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, OrderResponseDto.class));
            return orders;
        }
    }

    public void cancelOrder(String orderId) throws IOException {
        String url = getBaseUrl() + "/v2/orders/" + orderId;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("APCA-API-KEY-ID", getApiKey())
                .addHeader("APCA-API-SECRET-KEY", getApiSecret())
                .delete()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.code() != 204 && response.code() != 500) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    logger.error("Cancel order failed: {} - {}", response.code(), errorBody);
                    throw new IOException("Cancel failed: " + errorBody);
                }
            }
            logger.info("Order {} cancelled", orderId);
        }
    }

    private String buildOrderJson(OrderRequestDto req) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"symbol\":\"").append(req.symbol()).append("\",");
        sb.append("\"qty\":").append(req.qty() != null ? req.qty() : "null").append(",");
        sb.append("\"side\":\"").append(req.side()).append("\",");
        sb.append("\"type\":\"").append(req.type()).append("\",");
        sb.append("\"time_in_force\":\"").append(req.timeInForce() != null ? req.timeInForce() : "day").append("\"");

        if (req.limitPrice() != null) {
            sb.append(",\"limit_price\":").append(req.limitPrice());
        }
        if (req.stopPrice() != null) {
            sb.append(",\"stop_price\":").append(req.stopPrice());
        }
        if (req.clientOrderId() != null && !req.clientOrderId().isEmpty()) {
            sb.append(",\"client_order_id\":\"").append(req.clientOrderId()).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }
}
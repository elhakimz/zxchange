package com.zxchange.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxchange.model.dto.PositionDto;
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
public class PositionService {

    private static final Logger logger = LoggerFactory.getLogger(PositionService.class);
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

    public PositionService(SettingsRepository settingsRepository, EncryptionService encryptionService) {
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

    public List<PositionDto> getPositions() throws IOException {
        String url = getBaseUrl() + "/v2/positions";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("APCA-API-KEY-ID", getApiKey())
                .addHeader("APCA-API-SECRET-KEY", getApiSecret())
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Failed to get positions: {}", response.code());
                return new ArrayList<>();
            }
            String responseBody = response.body().string();
            List<PositionDto> positions = objectMapper.readValue(responseBody,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PositionDto.class));
            return positions;
        }
    }

    public void closePosition(String symbol) throws IOException {
        String url = getBaseUrl() + "/v2/positions/" + symbol;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("APCA-API-KEY-ID", getApiKey())
                .addHeader("APCA-API-SECRET-KEY", getApiSecret())
                .delete()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                logger.error("Failed to close position {}: {} - {}", symbol, response.code(), errorBody);
                throw new IOException("Close failed: " + errorBody);
            }
            logger.info("Position {} closed", symbol);
        }
    }
}
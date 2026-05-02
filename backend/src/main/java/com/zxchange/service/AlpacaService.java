package com.zxchange.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxchange.model.dto.AccountDto;
import com.zxchange.model.dto.BarsResponseDto;
import com.zxchange.model.entity.SettingsEntity;
import com.zxchange.repository.SettingsRepository;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AlpacaService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AlpacaService.class);
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SettingsRepository settingsRepository;
    private final EncryptionService encryptionService;

    @Value("${zxchange.alpaca.base-url}")
    private String baseUrl;

    @Value("${zxchange.alpaca.paper-base-url}")
    private String paperBaseUrl;

    @Value("${zxchange.alpaca.data-url:https://data.alpaca.markets}")
    private String dataUrl;

    @Value("${ALPACA_API_KEY:}")
    private String envApiKey;

    @Value("${ALPACA_API_SECRET:}")
    private String envApiSecret;

    @Value("${ALPACA_ENDPOINT:}")
    private String envEndpoint;

    public AlpacaService(SettingsRepository settingsRepository, EncryptionService encryptionService) {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.settingsRepository = settingsRepository;
        this.encryptionService = encryptionService;
    }

    public AccountDto getAccount() throws IOException {
        String apiKey = getApiKey();
        String apiSecret = getApiSecret();
        
        String url = getBaseUrl() + "/v2/account";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("APCA-API-KEY-ID", apiKey)
                .addHeader("APCA-API-SECRET-KEY", apiSecret)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return objectMapper.readValue(response.body().string(), AccountDto.class);
        }
    }

    public BarsResponseDto getBars(String symbol, String timeframe, String start, String end) throws IOException {
        String apiKey = getApiKey();
        String apiSecret = getApiSecret();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(dataUrl + "/v2/stocks/" + symbol + "/bars").newBuilder();
        urlBuilder.addQueryParameter("timeframe", timeframe);
        if (start != null) urlBuilder.addQueryParameter("start", start);
        if (end != null) urlBuilder.addQueryParameter("end", end);
        urlBuilder.addQueryParameter("limit", "1000");
        urlBuilder.addQueryParameter("adjustment", "raw");
        urlBuilder.addQueryParameter("feed", "iex");

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("APCA-API-KEY-ID", apiKey)
                .addHeader("APCA-API-SECRET-KEY", apiSecret)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                logger.error("Alpaca API error in getBars: {} - {}", response.code(), errorBody);
                throw new IOException("Unexpected code " + response.code() + " : " + errorBody);
            }
            String body = response.body().string();
            BarsResponseDto dto = objectMapper.readValue(body, BarsResponseDto.class);
            logger.info("Fetched {} bars for {}", dto.bars() != null ? dto.bars().size() : 0, symbol);
            return dto;
        }
    }

    private String getApiKey() {
        return (envApiKey != null && !envApiKey.isEmpty()) ? envApiKey : getSetting("ALPACA_API_KEY");
    }

    private String getApiSecret() {
        return (envApiSecret != null && !envApiSecret.isEmpty()) ? envApiSecret : getSetting("ALPACA_API_SECRET");
    }

    private String getBaseUrl() {
        if (envEndpoint != null && !envEndpoint.isEmpty()) {
            // Remove /v2 if present as we append it
            return envEndpoint.replace("/v2", "");
        }
        boolean isPaper = "true".equalsIgnoreCase(getSetting("ALPACA_IS_PAPER"));
        return isPaper ? paperBaseUrl : baseUrl;
    }

    private String getSetting(String key) {
        return settingsRepository.findByKey(key)
                .map(s -> s.isEncrypted() ? encryptionService.decrypt(s.getValue()) : s.getValue())
                .orElse("");
    }
}

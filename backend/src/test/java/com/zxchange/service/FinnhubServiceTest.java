package com.zxchange.service;

import com.zxchange.model.dto.BarsResponseDto;
import com.zxchange.model.dto.QuoteDto;
import com.zxchange.repository.SettingsRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FinnhubServiceTest {

    private MockWebServer mockWebServer;
    private FinnhubService finnhubService;

    @Mock
    private SettingsRepository settingsRepository;

    @Mock
    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        finnhubService = new FinnhubService(
            settingsRepository, 
            encryptionService, 
            mockWebServer.url("/").toString(), 
            "test-api-key"
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testGetQuote_Success() throws IOException, InterruptedException {
        // Arrange
        String json = "{\"c\": 150.0, \"d\": 2.1, \"dp\": 1.35, \"h\": 155.0, \"l\": 148.0, \"o\": 149.0, \"pc\": 147.9}";
        mockWebServer.enqueue(new MockResponse().setBody(json).setResponseCode(200));

        // Act
        QuoteDto quote = finnhubService.getQuote("AAPL");

        // Assert
        assertNotNull(quote);
        assertEquals(150.0, quote.bidPrice());
        assertEquals("AAPL", quote.symbol());

        RecordedRequest request = mockWebServer.takeRequest();
        assertTrue(request.getPath().contains("quote"));
        assertTrue(request.getPath().contains("symbol=AAPL"));
        assertTrue(request.getPath().contains("token=test-api-key"));
    }

    @Test
    void testGetBars_Stock_Success() throws IOException, InterruptedException {
        // Arrange
        String json = "{\"s\": \"ok\", \"t\": [1609459200], \"o\": [150.0], \"h\": [155.0], \"l\": [148.0], \"c\": [152.0], \"v\": [1000]}";
        mockWebServer.enqueue(new MockResponse().setBody(json).setResponseCode(200));

        // Act
        BarsResponseDto response = finnhubService.getBars("AAPL", "1D", null, null);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.bars().size());
        assertEquals(150.0, response.bars().get(0).open());

        RecordedRequest request = mockWebServer.takeRequest();
        assertTrue(request.getPath().contains("stock/candle"));
        assertTrue(request.getPath().contains("symbol=AAPL"));
    }

    @Test
    void testGetBars_Crypto_Success() throws IOException, InterruptedException {
        // Arrange
        String json = "{\"s\": \"ok\", \"t\": [1609459200], \"o\": [20000.0], \"h\": [21000.0], \"l\": [19000.0], \"c\": [20500.0], \"v\": [100]}";
        mockWebServer.enqueue(new MockResponse().setBody(json).setResponseCode(200));

        // Act
        BarsResponseDto response = finnhubService.getBars("BINANCE:BTCUSDT", "1D", null, null);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.bars().size());

        RecordedRequest request = mockWebServer.takeRequest();
        String path = request.getPath();
        assertNotNull(path);
        assertTrue(path.contains("crypto") && path.contains("candle"), "Path should contain 'crypto' and 'candle', but was: " + path);
        assertTrue(request.getRequestUrl().toString().contains("symbol=BINANCE%3ABTCUSDT"));
    }

    @Test
    void testGetBars_403_FallbackToMock() throws IOException {
        // Arrange
        mockWebServer.enqueue(new MockResponse().setResponseCode(403));

        // Act
        BarsResponseDto response = finnhubService.getBars("MSFT", "1Min", null, null);

        // Assert
        assertNotNull(response);
        assertFalse(response.bars().isEmpty()); // Should have generated mock bars
        assertEquals("MSFT", response.symbol());
    }

    @Test
    void testGetBars_NonOkStatus_FallbackToMock() throws IOException {
        // Arrange
        String json = "{\"s\": \"no_data\"}";
        mockWebServer.enqueue(new MockResponse().setBody(json).setResponseCode(200));

        // Act
        BarsResponseDto response = finnhubService.getBars("UNKNOWN", "1D", null, null);

        // Assert
        assertNotNull(response);
        assertFalse(response.bars().isEmpty());
    }
}

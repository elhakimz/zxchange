package com.zxchange.controller;

import com.zxchange.model.dto.BarsResponseDto;
import com.zxchange.service.FinnhubService;
import com.zxchange.websocket.FinnhubMarketDataWsClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/marketdata")
public class MarketDataController {

    private final FinnhubService finnhubService;
    private final FinnhubMarketDataWsClient wsClient;

    public MarketDataController(FinnhubService finnhubService, FinnhubMarketDataWsClient wsClient) {
        this.finnhubService = finnhubService;
        this.wsClient = wsClient;
    }

    @GetMapping("/bars")
    public ResponseEntity<?> getBars(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "1Min") String timeframe,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        try {
            // Ensure we are subscribed to real-time data for this symbol
            wsClient.subscribe(symbol);
            
            BarsResponseDto bars = finnhubService.getBars(symbol, timeframe, start, end);
            return ResponseEntity.ok(bars);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error fetching bars: " + e.getMessage());
        }
    }
}

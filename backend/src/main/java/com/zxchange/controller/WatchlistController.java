package com.zxchange.controller;

import com.zxchange.model.dto.WatchlistDto;
import com.zxchange.service.WatchlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/watchlists")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping
    public List<WatchlistDto> getAllWatchlists() {
        return watchlistService.getAllWatchlists();
    }

    @PostMapping
    public WatchlistDto createWatchlist(@RequestBody Map<String, String> request) {
        return watchlistService.createWatchlist(request.get("name"));
    }

    @PostMapping("/{id}/symbols")
    public WatchlistDto addSymbol(@PathVariable Long id, @RequestBody Map<String, String> request) {
        return watchlistService.addSymbolToWatchlist(id, request.get("symbol"));
    }

    @DeleteMapping("/{id}/symbols/{symbol}")
    public WatchlistDto removeSymbol(@PathVariable Long id, @PathVariable String symbol) {
        return watchlistService.removeSymbolFromWatchlist(id, symbol);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWatchlist(@PathVariable Long id) {
        watchlistService.deleteWatchlist(id);
        return ResponseEntity.ok().build();
    }
}

package com.zxchange.service;

import com.zxchange.model.dto.WatchlistDto;
import com.zxchange.model.dto.WatchlistSymbolDto;
import com.zxchange.model.entity.WatchlistEntity;
import com.zxchange.model.entity.WatchlistSymbolEntity;
import com.zxchange.repository.WatchlistRepository;
import com.zxchange.websocket.FinnhubMarketDataWsClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final FinnhubMarketDataWsClient finnhubWsClient;

    public WatchlistService(WatchlistRepository watchlistRepository, FinnhubMarketDataWsClient finnhubWsClient) {
        this.watchlistRepository = watchlistRepository;
        this.finnhubWsClient = finnhubWsClient;
    }

    @Transactional(readOnly = true)
    public List<WatchlistDto> getAllWatchlists() {
        return watchlistRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public WatchlistDto createWatchlist(String name) {
        WatchlistEntity entity = new WatchlistEntity();
        entity.setName(name);
        return mapToDto(watchlistRepository.save(entity));
    }

    @Transactional
    public WatchlistDto addSymbolToWatchlist(Long watchlistId, String symbol) {
        WatchlistEntity watchlist = watchlistRepository.findById(watchlistId)
                .orElseThrow(() -> new RuntimeException("Watchlist not found"));

        boolean exists = watchlist.getSymbols().stream()
                .anyMatch(s -> s.getSymbol().equalsIgnoreCase(symbol));

        if (!exists) {
            WatchlistSymbolEntity symbolEntity = new WatchlistSymbolEntity();
            symbolEntity.setWatchlist(watchlist);
            symbolEntity.setSymbol(symbol.toUpperCase());
            symbolEntity.setPositionIndex(watchlist.getSymbols().size());
            watchlist.getSymbols().add(symbolEntity);
            watchlistRepository.save(watchlist);
            
            // Trigger Finnhub WS subscription
            finnhubWsClient.subscribe(symbol.toUpperCase());
        }

        return mapToDto(watchlist);
    }

    @Transactional
    public WatchlistDto removeSymbolFromWatchlist(Long watchlistId, String symbol) {
        WatchlistEntity watchlist = watchlistRepository.findById(watchlistId)
                .orElseThrow(() -> new RuntimeException("Watchlist not found"));

        watchlist.getSymbols().removeIf(s -> s.getSymbol().equalsIgnoreCase(symbol));
        return mapToDto(watchlistRepository.save(watchlist));
    }

    @Transactional
    public void deleteWatchlist(Long id) {
        watchlistRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<String> getAllSymbols() {
        return watchlistRepository.findAll().stream()
                .flatMap(w -> w.getSymbols().stream())
                .map(WatchlistSymbolEntity::getSymbol)
                .distinct()
                .collect(Collectors.toList());
    }

    private WatchlistDto mapToDto(WatchlistEntity entity) {
        List<WatchlistSymbolDto> symbolDtos = entity.getSymbols().stream()
                .map(s -> new WatchlistSymbolDto(s.getId(), s.getSymbol(), s.getPositionIndex(), s.getAddedAt()))
                .collect(Collectors.toList());

        return new WatchlistDto(
                entity.getId(),
                entity.getName(),
                entity.getCreatedAt(),
                symbolDtos
        );
    }
}

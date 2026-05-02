package com.zxchange.config;

import com.zxchange.model.entity.WatchlistEntity;
import com.zxchange.model.entity.WatchlistSymbolEntity;
import com.zxchange.repository.WatchlistRepository;
import com.zxchange.websocket.FinnhubMarketDataWsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(1)
public class WatchlistSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(WatchlistSeeder.class);
    private static final List<String> DEFAULT_SYMBOLS = List.of("AAPL", "MSFT", "GOOGL", "AMZN", "TSLA");

    private final WatchlistRepository watchlistRepository;
    private final FinnhubMarketDataWsClient finnhubWsClient;

    public WatchlistSeeder(WatchlistRepository watchlistRepository, FinnhubMarketDataWsClient finnhubWsClient) {
        this.watchlistRepository = watchlistRepository;
        this.finnhubWsClient = finnhubWsClient;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!watchlistRepository.findAll().isEmpty()) {
            logger.info("Watchlists already exist, skipping seed");
            return;
        }

        logger.info("Seeding default watchlist with symbols: {}", DEFAULT_SYMBOLS);
        WatchlistEntity watchlist = new WatchlistEntity();
        watchlist.setName("Default");

        for (int i = 0; i < DEFAULT_SYMBOLS.size(); i++) {
            WatchlistSymbolEntity symbol = new WatchlistSymbolEntity();
            symbol.setWatchlist(watchlist);
            symbol.setSymbol(DEFAULT_SYMBOLS.get(i));
            symbol.setPositionIndex(i);
            watchlist.getSymbols().add(symbol);
        }

        watchlistRepository.save(watchlist);
        finnhubWsClient.resubscribe(DEFAULT_SYMBOLS);
        logger.info("Default watchlist seeded with {} symbols", DEFAULT_SYMBOLS.size());
    }
}

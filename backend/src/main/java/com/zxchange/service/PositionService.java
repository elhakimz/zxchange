package com.zxchange.service;

import com.zxchange.model.dto.PositionDto;
import com.zxchange.model.dto.QuoteDto;
import com.zxchange.model.entity.MockAccountEntity;
import com.zxchange.model.entity.MockPositionEntity;
import com.zxchange.repository.MockAccountRepository;
import com.zxchange.repository.MockPositionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PositionService {

    private static final Logger logger = LoggerFactory.getLogger(PositionService.class);
    private final MockPositionRepository positionRepository;
    private final FinnhubService finnhubService;
    private final MockAccountRepository accountRepository;

    public PositionService(MockPositionRepository positionRepository,
                           FinnhubService finnhubService,
                           MockAccountRepository accountRepository) {
        this.positionRepository = positionRepository;
        this.finnhubService = finnhubService;
        this.accountRepository = accountRepository;
    }

    public List<PositionDto> getPositions() {
        return positionRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public void closePosition(String symbol) throws IOException {
        MockPositionEntity pos = positionRepository.findBySymbol(symbol)
                .orElseThrow(() -> new IOException("Position not found: " + symbol));
        
        QuoteDto quote = finnhubService.getQuote(symbol);
        double fillPrice = quote.bidPrice();
        double proceeds = fillPrice * pos.getQty();

        MockAccountEntity account = accountRepository.findById("PRIMARY")
                .orElseGet(() -> accountRepository.save(new MockAccountEntity()));
        
        account.setCash(account.getCash() + proceeds);
        accountRepository.save(account);
        positionRepository.delete(pos);

        logger.info("Mock position {} closed at {}", symbol, fillPrice);
    }

    private PositionDto mapToDto(MockPositionEntity entity) {
        double currentPrice = 0.0;
        try {
            currentPrice = finnhubService.getQuote(entity.getSymbol()).bidPrice();
        } catch (Exception e) {
            currentPrice = entity.getAvgEntryPrice();
        }

        double marketValue = currentPrice * entity.getQty();
        double costBasis = entity.getAvgEntryPrice() * entity.getQty();
        double unrealizedPl = marketValue - costBasis;
        double unrealizedPlpc = (costBasis != 0) ? unrealizedPl / costBasis : 0.0;

        return new PositionDto(
                entity.getSymbol(),
                entity.getQty(),
                entity.getQty() >= 0 ? "long" : "short",
                entity.getAvgEntryPrice(),
                currentPrice,
                marketValue,
                costBasis,
                unrealizedPl,
                unrealizedPlpc
        );
    }
}

package com.zxchange.service;

import com.zxchange.model.dto.AccountDto;
import com.zxchange.model.entity.MockAccountEntity;
import com.zxchange.model.entity.MockPositionEntity;
import com.zxchange.model.entity.PortfolioSnapshotEntity;
import com.zxchange.repository.MockAccountRepository;
import com.zxchange.repository.MockPositionRepository;
import com.zxchange.repository.PortfolioSnapshotRepository;
import com.zxchange.websocket.StompBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioService {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioService.class);
    private final FinnhubService finnhubService;
    private final StompBroadcaster stompBroadcaster;
    private final PortfolioSnapshotRepository snapshotRepository;
    private final MockAccountRepository accountRepository;
    private final MockPositionRepository positionRepository;

    private AccountDto lastAccount;

    public PortfolioService(FinnhubService finnhubService, 
                            StompBroadcaster stompBroadcaster, 
                            PortfolioSnapshotRepository snapshotRepository,
                            MockAccountRepository accountRepository,
                            MockPositionRepository positionRepository) {
        this.finnhubService = finnhubService;
        this.stompBroadcaster = stompBroadcaster;
        this.snapshotRepository = snapshotRepository;
        this.accountRepository = accountRepository;
        this.positionRepository = positionRepository;
    }

    @Scheduled(fixedRate = 5000)
    public void pollAccount() {
        try {
            MockAccountEntity accountEntity = accountRepository.findById("PRIMARY")
                    .orElseGet(() -> accountRepository.save(new MockAccountEntity()));

            double marketValue = 0.0;
            List<MockPositionEntity> positions = positionRepository.findAll();
            for (MockPositionEntity pos : positions) {
                try {
                    marketValue += pos.getQty() * finnhubService.getQuote(pos.getSymbol()).bidPrice();
                } catch (Exception e) {
                    marketValue += pos.getQty() * pos.getAvgEntryPrice();
                }
            }

            double equity = accountEntity.getCash() + marketValue;
            
            AccountDto account = new AccountDto(
                    "mock-id",
                    "MOCK001",
                    "active",
                    "USD",
                    String.valueOf(equity), // buying power = equity for simple mock
                    String.valueOf(accountEntity.getCash()),
                    String.valueOf(equity),
                    String.valueOf(equity),
                    String.valueOf(marketValue),
                    "0",
                    "0",
                    "0",
                    String.valueOf(accountEntity.getStartingEquity()),
                    "0"
            );

            this.lastAccount = account;
            stompBroadcaster.broadcastAccount(account);
        } catch (Exception e) {
            logger.error("Error updating mock portfolio: {}", e.getMessage());
        }
    }

    public AccountDto getLatestAccount() {
        if (lastAccount == null) {
            pollAccount(); // Initial fetch
        }
        return lastAccount;
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    public void saveSnapshot() {
        if (lastAccount == null) return;

        try {
            PortfolioSnapshotEntity snapshot = new PortfolioSnapshotEntity();
            snapshot.setEquity(Double.parseDouble(lastAccount.equity()));
            snapshot.setCash(Double.parseDouble(lastAccount.cash()));
            
            double equity = Double.parseDouble(lastAccount.equity());
            double startingEquity = Double.parseDouble(lastAccount.lastEquity());
            snapshot.setPlDay(equity - startingEquity);

            snapshotRepository.save(snapshot);
            logger.info("Saved mock portfolio snapshot: Equity={}", snapshot.getEquity());
        } catch (Exception e) {
            logger.error("Error saving portfolio snapshot: {}", e.getMessage());
        }
    }
}

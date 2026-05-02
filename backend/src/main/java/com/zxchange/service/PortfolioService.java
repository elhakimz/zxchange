package com.zxchange.service;

import com.zxchange.model.dto.AccountDto;
import com.zxchange.model.entity.PortfolioSnapshotEntity;
import com.zxchange.repository.PortfolioSnapshotRepository;
import com.zxchange.websocket.StompBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PortfolioService {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioService.class);
    private final AlpacaService alpacaService;
    private final StompBroadcaster stompBroadcaster;
    private final PortfolioSnapshotRepository snapshotRepository;

    private AccountDto lastAccount;

    public PortfolioService(AlpacaService alpacaService, 
                            StompBroadcaster stompBroadcaster, 
                            PortfolioSnapshotRepository snapshotRepository) {
        this.alpacaService = alpacaService;
        this.stompBroadcaster = stompBroadcaster;
        this.snapshotRepository = snapshotRepository;
    }

    @Scheduled(fixedRate = 5000)
    public void pollAccount() {
        try {
            AccountDto account = alpacaService.getAccount();
            this.lastAccount = account;
            stompBroadcaster.broadcastAccount(account);
        } catch (IOException e) {
            logger.error("Error polling account from Alpaca: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    public void saveSnapshot() {
        if (lastAccount == null) return;

        try {
            PortfolioSnapshotEntity snapshot = new PortfolioSnapshotEntity();
            snapshot.setEquity(Double.parseDouble(lastAccount.equity()));
            snapshot.setCash(Double.parseDouble(lastAccount.cash()));
            
            // Calculate Day P&L if not directly provided in a simple way
            // or if we want to store what was in the account DTO
            double equity = Double.parseDouble(lastAccount.equity());
            double lastEquity = Double.parseDouble(lastAccount.lastEquity());
            snapshot.setPlDay(equity - lastEquity);

            snapshotRepository.save(snapshot);
            logger.info("Saved portfolio snapshot: Equity={}", snapshot.getEquity());
        } catch (Exception e) {
            logger.error("Error saving portfolio snapshot: {}", e.getMessage());
        }
    }
}

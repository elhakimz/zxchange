package com.zxchange.controller;

import com.zxchange.model.dto.AccountDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final com.zxchange.service.PortfolioService portfolioService;
    private final com.zxchange.repository.PortfolioSnapshotRepository snapshotRepository;

    public AccountController(com.zxchange.service.PortfolioService portfolioService, 
                             com.zxchange.repository.PortfolioSnapshotRepository snapshotRepository) {
        this.portfolioService = portfolioService;
        this.snapshotRepository = snapshotRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAccount() {
        return ResponseEntity.ok(portfolioService.getLatestAccount());
    }

    @GetMapping("/snapshots")
    public ResponseEntity<?> getSnapshots() {
        return ResponseEntity.ok(snapshotRepository.findLatestSnapshots());
    }
}

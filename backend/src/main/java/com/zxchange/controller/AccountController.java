package com.zxchange.controller;

import com.zxchange.model.dto.AccountDto;
import com.zxchange.service.AlpacaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AlpacaService alpacaService;
    private final com.zxchange.repository.PortfolioSnapshotRepository snapshotRepository;

    public AccountController(AlpacaService alpacaService, 
                             com.zxchange.repository.PortfolioSnapshotRepository snapshotRepository) {
        this.alpacaService = alpacaService;
        this.snapshotRepository = snapshotRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAccount() {
        try {
            AccountDto account = alpacaService.getAccount();
            return ResponseEntity.ok(account);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error fetching account from Alpaca: " + e.getMessage());
        }
    }

    @GetMapping("/snapshots")
    public ResponseEntity<?> getSnapshots() {
        return ResponseEntity.ok(snapshotRepository.findLatestSnapshots());
    }
}

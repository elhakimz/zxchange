package com.zxchange.controller;

import com.zxchange.model.dto.PositionDto;
import com.zxchange.service.PositionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    private static final Logger logger = LoggerFactory.getLogger(PositionController.class);
    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    public ResponseEntity<?> getPositions() {
        try {
            List<PositionDto> positions = positionService.getPositions();
            return ResponseEntity.ok(positions);
        } catch (Exception e) {
            logger.error("Failed to get positions: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{symbol}")
    public ResponseEntity<?> closePosition(@PathVariable String symbol) {
        try {
            positionService.closePosition(symbol);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to close position {}: {}", symbol, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

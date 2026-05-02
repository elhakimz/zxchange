package com.zxchange.controller;

import com.zxchange.model.dto.OrderRequestDto;
import com.zxchange.model.dto.OrderResponseDto;
import com.zxchange.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequestDto request) {
        try {
            logger.info("Received order request: symbol={}, side={}, type={}, qty={}",
                    request.symbol(), request.side(), request.type(), request.qty());
            OrderResponseDto order = orderService.placeOrder(request);
            return ResponseEntity.ok(order);
        } catch (IOException e) {
            logger.error("Failed to place order: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getOpenOrders() {
        try {
            List<OrderResponseDto> orders = orderService.getOpenOrders();
            return ResponseEntity.ok(orders);
        } catch (IOException e) {
            logger.error("Failed to get orders: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderId) {
        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            logger.error("Failed to cancel order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
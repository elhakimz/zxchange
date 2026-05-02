package com.zxchange.service;

import com.zxchange.model.dto.OrderRequestDto;
import com.zxchange.model.dto.OrderResponseDto;
import com.zxchange.model.dto.QuoteDto;
import com.zxchange.model.entity.MockAccountEntity;
import com.zxchange.model.entity.MockPositionEntity;
import com.zxchange.model.entity.OrderHistoryEntity;
import com.zxchange.repository.MockAccountRepository;
import com.zxchange.repository.MockPositionRepository;
import com.zxchange.repository.OrderHistoryRepository;
import com.zxchange.websocket.StompBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderHistoryRepository orderRepository;
    private final MockAccountRepository accountRepository;
    private final MockPositionRepository positionRepository;
    private final FinnhubService finnhubService;
    private final StompBroadcaster stompBroadcaster;

    public OrderService(OrderHistoryRepository orderRepository,
                        MockAccountRepository accountRepository,
                        MockPositionRepository positionRepository,
                        FinnhubService finnhubService,
                        StompBroadcaster stompBroadcaster) {
        this.orderRepository = orderRepository;
        this.accountRepository = accountRepository;
        this.positionRepository = positionRepository;
        this.finnhubService = finnhubService;
        this.stompBroadcaster = stompBroadcaster;
    }

    @Transactional
    public OrderResponseDto placeOrder(OrderRequestDto request) throws IOException {
        logger.info("Placing mock order: {} {}", request.side(), request.symbol());

        OrderHistoryEntity entity = new OrderHistoryEntity();
        entity.setBrokerOrderId("mock-" + UUID.randomUUID().toString());
        entity.setClientOrderId(request.clientOrderId());
        entity.setSymbol(request.symbol().toUpperCase());
        entity.setSide(request.side());
        entity.setType(request.type());
        entity.setTimeInForce(request.timeInForce() != null ? request.timeInForce() : "day");
        entity.setQty(request.qty());
        entity.setLimitPrice(request.limitPrice());
        entity.setStopPrice(request.stopPrice());
        entity.setStatus("accepted");
        entity.setSubmittedAt(java.time.LocalDateTime.now());
        
        entity = orderRepository.save(entity);

        if ("market".equalsIgnoreCase(request.type())) {
            executeMockOrder(entity);
        }

        return mapToDto(entity);
    }

    private void executeMockOrder(OrderHistoryEntity order) throws IOException {
        QuoteDto quote = finnhubService.getQuote(order.getSymbol());
        double fillPrice = quote.bidPrice(); 
        
        MockAccountEntity account = accountRepository.findById("PRIMARY")
                .orElseGet(() -> accountRepository.save(new MockAccountEntity()));

        double cost = fillPrice * order.getQty();
        if ("buy".equalsIgnoreCase(order.getSide())) {
            if (account.getCash() < cost) {
                order.setStatus("rejected");
                orderRepository.save(order);
                logger.warn("Order rejected: insufficient cash");
                return;
            }
            account.setCash(account.getCash() - cost);
            updatePosition(order.getSymbol(), order.getQty(), fillPrice);
        } else {
            account.setCash(account.getCash() + cost);
            updatePosition(order.getSymbol(), -order.getQty(), fillPrice);
        }

        accountRepository.save(account);
        
        order.setStatus("filled");
        order.setFilledQty(order.getQty());
        order.setFilledAvgPrice(fillPrice);
        order.setFilledAt(java.time.LocalDateTime.now());
        orderRepository.save(order);

        stompBroadcaster.broadcastOrderUpdate(mapToDto(order));
        logger.info("Mock order filled: {} @ {}", order.getSymbol(), fillPrice);
    }

    private void updatePosition(String symbol, Double qtyDelta, Double price) {
        MockPositionEntity pos = positionRepository.findBySymbol(symbol)
                .orElse(new MockPositionEntity());
        
        if (pos.getSymbol() == null) {
            pos.setSymbol(symbol);
            pos.setQty(0.0);
            pos.setAvgEntryPrice(0.0);
        }

        double oldQty = pos.getQty();
        double newQty = oldQty + qtyDelta;

        if (newQty <= 0) {
            positionRepository.delete(pos);
            return;
        }

        if (qtyDelta > 0) {
            double totalCost = (oldQty * pos.getAvgEntryPrice()) + (qtyDelta * price);
            pos.setAvgEntryPrice(totalCost / newQty);
        }
        
        pos.setQty(newQty);
        positionRepository.save(pos);
    }

    public List<OrderResponseDto> getOpenOrders() {
        return orderRepository.findByStatus("accepted").stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public void cancelOrder(String orderId) {
        orderRepository.findAll().stream()
                .filter(o -> o.getBrokerOrderId().equals(orderId))
                .findFirst()
                .ifPresent(o -> {
                    o.setStatus("canceled");
                    o.setCanceledAt(java.time.LocalDateTime.now());
                    orderRepository.save(o);
                });
    }

    private OrderResponseDto mapToDto(OrderHistoryEntity entity) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(entity.getBrokerOrderId());
        dto.setClientOrderId(entity.getClientOrderId());
        dto.setSymbol(entity.getSymbol());
        dto.setSide(entity.getSide());
        dto.setType(entity.getType());
        dto.setTimeInForce(entity.getTimeInForce());
        dto.setStatus(entity.getStatus());
        dto.setSubmittedAt(entity.getSubmittedAt() != null ? entity.getSubmittedAt().toString() : null);
        dto.setFilledAt(entity.getFilledAt() != null ? entity.getFilledAt().toString() : null);
        dto.setFilledQty(entity.getFilledQty() != null ? entity.getFilledQty().intValue() : 0);
        dto.setFilledAvgPrice(entity.getFilledAvgPrice() != null ? entity.getFilledAvgPrice() : 0.0);
        dto.setLimitPrice(entity.getLimitPrice() != null ? entity.getLimitPrice() : 0.0);
        dto.setStopPrice(entity.getStopPrice() != null ? entity.getStopPrice() : 0.0);
        return dto;
    }
}

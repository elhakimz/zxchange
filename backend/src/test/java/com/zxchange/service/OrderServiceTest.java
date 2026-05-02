package com.zxchange.service;

import com.zxchange.model.dto.OrderRequestDto;
import com.zxchange.model.dto.OrderResponseDto;
import com.zxchange.model.dto.QuoteDto;
import com.zxchange.model.entity.MockAccountEntity;
import com.zxchange.model.entity.OrderHistoryEntity;
import com.zxchange.repository.MockAccountRepository;
import com.zxchange.repository.MockPositionRepository;
import com.zxchange.repository.OrderHistoryRepository;
import com.zxchange.websocket.StompBroadcaster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderHistoryRepository orderRepository;
    @Mock
    private MockAccountRepository accountRepository;
    @Mock
    private MockPositionRepository positionRepository;
    @Mock
    private FinnhubService finnhubService;
    @Mock
    private StompBroadcaster stompBroadcaster;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderService(orderRepository, accountRepository, positionRepository, finnhubService, stompBroadcaster);
    }

    @Test
    void testPlaceMarketOrder_Success() throws IOException {
        // Arrange
        OrderRequestDto request = new OrderRequestDto("AAPL", 10.0, "buy", "market", "day", null, null, "test-client-id");
        
        MockAccountEntity account = new MockAccountEntity();
        account.setCash(100000.0);
        
        QuoteDto quote = new QuoteDto("AAPL", 150.0, 0, null, 150.0, 0, null, "123456789");

        when(orderRepository.save(any(OrderHistoryEntity.class))).thenAnswer(i -> i.getArguments()[0]);
        when(finnhubService.getQuote("AAPL")).thenReturn(quote);
        when(accountRepository.findById("PRIMARY")).thenReturn(Optional.of(account));
        when(positionRepository.findBySymbol("AAPL")).thenReturn(Optional.empty());

        // Act
        OrderResponseDto response = orderService.placeOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals("filled", response.getStatus());
        assertEquals(150.0, response.getFilledAvgPrice());
        assertEquals(10, response.getFilledQty());
        
        // Verify cash deducted: 100000 - (150 * 10) = 98500
        assertEquals(98500.0, account.getCash());
        
        verify(accountRepository).save(account);
        verify(positionRepository).save(any());
        verify(stompBroadcaster).broadcastOrderUpdate(any());
    }

    @Test
    void testPlaceMarketOrder_InsufficientCash() throws IOException {
        // Arrange
        OrderRequestDto request = new OrderRequestDto("AAPL", 10.0, "buy", "market", "day", null, null, "test-client-id");
        
        MockAccountEntity account = new MockAccountEntity();
        account.setCash(100.0); // Not enough for 10 shares @ 150
        
        QuoteDto quote = new QuoteDto("AAPL", 150.0, 0, null, 150.0, 0, null, "123456789");

        when(orderRepository.save(any(OrderHistoryEntity.class))).thenAnswer(i -> i.getArguments()[0]);
        when(finnhubService.getQuote("AAPL")).thenReturn(quote);
        when(accountRepository.findById("PRIMARY")).thenReturn(Optional.of(account));

        // Act
        OrderResponseDto response = orderService.placeOrder(request);

        // Assert
        assertEquals("rejected", response.getStatus());
        assertEquals(100.0, account.getCash()); // Cash should not change
        verify(positionRepository, never()).save(any());
    }
}

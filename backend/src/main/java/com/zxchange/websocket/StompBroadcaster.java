package com.zxchange.websocket;

import com.zxchange.model.dto.BarDto;
import com.zxchange.model.dto.QuoteDto;
import com.zxchange.model.dto.OrderResponseDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class StompBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public StompBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastQuote(QuoteDto quote) {
        messagingTemplate.convertAndSend("/topic/quotes/" + quote.symbol(), quote);
    }

    public void broadcastBar(BarDto bar, String timeframe) {
        messagingTemplate.convertAndSend("/topic/bars/" + bar.symbol() + "/" + timeframe, bar);
    }

    public void broadcastAccount(com.zxchange.model.dto.AccountDto account) {
        messagingTemplate.convertAndSend("/topic/account", account);
    }

    public void broadcastOrderUpdate(OrderResponseDto order) {
        messagingTemplate.convertAndSend("/topic/orders", order);
    }

    public void broadcastAccountUpdate(String accountJson) {
        messagingTemplate.convertAndSend("/topic/account", accountJson);
    }

    public void broadcastTradeUpdate(String tradeJson) {
        messagingTemplate.convertAndSend("/topic/trades", tradeJson);
    }

    public void broadcastQuoteUpdate(String quoteJson) {
        messagingTemplate.convertAndSend("/topic/quotes/updates", quoteJson);
    }

    public void broadcastLog(String message) {
        messagingTemplate.convertAndSend("/topic/logs", message);
    }
}

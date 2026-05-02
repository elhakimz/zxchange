import { useEffect } from 'react';
import { stompClient } from './stompClient';
import { useMarketStore } from '../store/market.store';
import type { Quote, Bar, Timeframe } from '../types/market.types';

export const useMarketStream = (symbol: string, timeframe: Timeframe) => {
  const updateQuote = useMarketStore((state) => state.updateQuote);
  const appendBar = useMarketStore((state) => state.appendBar);
  const updateAccount = useMarketStore((state) => state.updateAccount);
  const connected = useMarketStore((state) => state.connected);

  useEffect(() => {
    if (!connected) return;

    const accountSub = stompClient.subscribe('/topic/account', (message) => {
      const account = JSON.parse(message.body);
      updateAccount(account);
    });

    if (!symbol) return () => accountSub.unsubscribe();

    const quoteSub = stompClient.subscribe(`/topic/quotes/${symbol}`, (message) => {
      const quote: Quote = JSON.parse(message.body);
      updateQuote(quote);
    });

    const barSub = stompClient.subscribe(`/topic/bars/${symbol}/${timeframe}`, (message) => {
      console.log(`[STOMP] Bar received for ${symbol}/${timeframe}:`, message.body);
      const b: any = JSON.parse(message.body);
      
      const bar: Bar = {
        symbol: b.S || b.symbol || symbol,
        timestamp: b.t || b.timestamp,
        open: b.o || b.open,
        high: b.h || b.high,
        low: b.l || b.low,
        close: b.c || b.close,
        volume: b.v || b.volume,
        vwap: b.vw || b.vwap,
        tradeCount: b.n || b.tradeCount
      };
      
      appendBar(symbol, timeframe, bar);
    });

    return () => {
      accountSub.unsubscribe();
      quoteSub.unsubscribe();
      barSub.unsubscribe();
    };
  }, [symbol, timeframe, updateQuote, appendBar, updateAccount, connected]);
};

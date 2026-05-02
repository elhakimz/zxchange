import { useEffect, useRef } from 'react';
import { useMarketStore, EMPTY_BARS } from '../store/market.store';
import type { Bar, Timeframe, Quote } from '../types/market.types';

export const useChartUpdateScheduler = (symbol: string, timeframe: Timeframe) => {
  const quote = useMarketStore((state) => state.quotes[symbol]);
  const bars = useMarketStore((state) => state.bars[`${symbol}_${timeframe}`] || EMPTY_BARS);
  const appendBar = useMarketStore((state) => state.appendBar);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const lastBarRef = useRef<Bar | null>(null);
  const quoteRef = useRef<Quote | null>(null);

  useEffect(() => {
    quoteRef.current = quote || null;
  }, [quote]);

  useEffect(() => {
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }

    if (!symbol) return;

    const getTickInterval = (tf: Timeframe) => {
      switch (tf) {
        case '1Sec': return 1_000;
        case '30Sec': return 5_000;
        case '1Min': return 10_000;
        default: return 0;
      }
    };

    const interval = getTickInterval(timeframe);
    if (interval === 0) return;

    timerRef.current = setInterval(() => {
      const currentQuote = quoteRef.current;
      if (currentQuote) {
        const price = currentQuote.askPrice || currentQuote.bidPrice;
        if (price > 0) {
          const liveBar: Bar = {
            symbol,
            timestamp: new Date().toISOString(),
            open: currentQuote.bidPrice,
            high: Math.max(currentQuote.bidPrice, currentQuote.askPrice),
            low: Math.min(currentQuote.bidPrice, currentQuote.askPrice),
            close: currentQuote.askPrice,
            volume: 0,
            vwap: (currentQuote.bidPrice + currentQuote.askPrice) / 2,
            tradeCount: 0,
          };
          appendBar(symbol, timeframe, liveBar);
        }
      } else if (lastBarRef.current && (timeframe === '1Sec' || timeframe === '30Sec')) {
        appendBar(symbol, timeframe, lastBarRef.current);
      }
    }, interval);

    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [symbol, timeframe, appendBar]);

  useEffect(() => {
    if (bars.length > 0) {
      lastBarRef.current = bars[bars.length - 1];
    }
  }, [bars]);
};
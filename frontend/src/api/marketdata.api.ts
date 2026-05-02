import { useQuery } from '@tanstack/react-query';
import client from './client';
import type { Bar, Timeframe } from '../types/market.types';
import { useMarketStore } from '../store/market.store';
import { useEffect } from 'react';

export const useBars = (symbol: string, timeframe: Timeframe) => {
  const setHistoricalBars = useMarketStore((state) => state.setHistoricalBars);

  const getRefetchInterval = (tf: Timeframe) => {
    switch (tf) {
      case '1Sec': return 2000;
      case '30Sec': return 5000;
      case '1Min': return 10000;
      case '15Min':
      case '30Min': return 30000;
      default: return 60000;
    }
  };

  const query = useQuery({
    queryKey: ['bars', symbol, timeframe],
    queryFn: async () => {
      try {
        console.log(`[API] Fetching bars for ${symbol} @ ${timeframe}...`);
        const { data } = await client.get<any>('/marketdata/bars', {
          params: { symbol, timeframe },
        });
        
        // Map BarDto fields from backend to Bar interface in frontend
        const bars: Bar[] = (data.bars || []).map((b: any) => ({
          symbol: b.symbol || symbol,
          timestamp: b.t || b.timestamp,
          open: b.o || b.open,
          high: b.h || b.high,
          low: b.l || b.low,
          close: b.c || b.close,
          volume: b.v || b.volume,
          vwap: b.vw || b.vwap,
          tradeCount: b.n || b.tradeCount
        }));
        
        return bars;
      } catch (error) {
        console.error(`Error fetching bars for ${symbol} @ ${timeframe}:`, error);
        return [];
      }
    },
    enabled: !!symbol,
    refetchInterval: getRefetchInterval(timeframe),
  });

  useEffect(() => {
    if (query.data) {
      setHistoricalBars(symbol, timeframe, query.data);
    }
  }, [query.data, symbol, timeframe, setHistoricalBars]);

  return query;
};

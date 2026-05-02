import { create } from 'zustand';
import type { Quote, Bar, Timeframe } from '../types/market.types';
import type { Account } from '../api/account.api';

interface MarketState {
  selectedSymbol: string;
  selectedTimeframe: Timeframe;
  quotes: Record<string, Quote>;
  bars: Record<string, Bar[]>;
  account: Account | null;
  connected: boolean;
  connectionError: string | null;
  
  setSelectedSymbol: (symbol: string) => void;
  setSelectedTimeframe: (timeframe: Timeframe) => void;
  updateQuote: (quote: Quote) => void;
  setHistoricalBars: (symbol: string, timeframe: Timeframe, bars: Bar[]) => void;
  appendBar: (symbol: string, timeframe: Timeframe, bar: Bar) => void;
  updateAccount: (account: Account) => void;
  setConnected: (connected: boolean) => void;
  setConnectionError: (error: string | null) => void;
}

export const EMPTY_BARS: Bar[] = [];

export const useMarketStore = create<MarketState>((set) => ({
  selectedSymbol: 'AAPL',
  selectedTimeframe: '1Min',
  quotes: {},
  bars: {},
  account: null,
  connected: false,
  connectionError: null,

  setSelectedSymbol: (symbol) => set({ selectedSymbol: symbol }),
  setSelectedTimeframe: (timeframe) => set({ selectedTimeframe: timeframe }),
  
  updateQuote: (quote) => set((state) => ({
    quotes: { ...state.quotes, [quote.symbol]: quote }
  })),

  setHistoricalBars: (symbol, timeframe, bars) => set((state) => ({
    bars: { ...state.bars, [`${symbol}_${timeframe}`]: bars }
  })),

  appendBar: (symbol, timeframe, bar) => set((state) => {
    const key = `${symbol}_${timeframe}`;
    const existingBars = state.bars[key] || EMPTY_BARS;
    const lastBar = existingBars[existingBars.length - 1];
    
    // Normalize timestamps to seconds for comparison
    const lastTime = lastBar ? Math.floor(new Date(lastBar.timestamp).getTime() / 1000) : 0;
    const newTime = Math.floor(new Date(bar.timestamp).getTime() / 1000);

    if (lastBar && lastTime === newTime) {
      const newBars = [...existingBars];
      newBars[newBars.length - 1] = bar;
      return { bars: { ...state.bars, [key]: newBars } };
    }
    return { bars: { ...state.bars, [key]: [...existingBars, bar] } };
  }),

  updateAccount: (account) => set({ account }),

  setConnected: (connected) => set({ connected }),
  
  setConnectionError: (error) => set({ connectionError: error }),
}));

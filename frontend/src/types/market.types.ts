export type Timeframe = '1Sec' | '30Sec' | '1Min' | '15Min' | '30Min' | '1Hour' | '12Hour' | '1Day' | '1Week';

export type ChartType = 'candle' | 'line' | 'baseline' | 'area' | 'bar';

export interface Quote {
  symbol: string;
  bidPrice: number;
  bidSize: number;
  askPrice: number;
  askSize: number;
  timestamp: string;
  change?: number;
  changePercent?: number;
}

export interface Bar {
  symbol: string;
  timestamp: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
  vwap: number;
  tradeCount: number;
}

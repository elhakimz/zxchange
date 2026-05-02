import React, { useEffect, useRef, useMemo } from 'react';
import { createChart, ColorType, CrosshairMode, CandlestickSeries } from 'lightweight-charts';
import type { IChartApi, ISeriesApi, UTCTimestamp } from 'lightweight-charts';
import { useMarketStore, EMPTY_BARS } from '../../store/market.store';
import type { Timeframe } from '../../types/market.types';

interface CandlestickChartProps {
  symbol: string;
  timeframe: Timeframe;
}

export const CandlestickChart: React.FC<CandlestickChartProps> = ({ symbol, timeframe }) => {
  const chartContainerRef = useRef<HTMLDivElement>(null);
  const chartRef = useRef<IChartApi | null>(null);
  const seriesRef = useRef<ISeriesApi<'Candlestick'> | null>(null);
  const lastTimestampRef = useRef<number>(0);
  
  // Keep track of the current symbol/timeframe to detect changes
  const currentKeyRef = useRef<string>('');
  
  const bars = useMarketStore((state) => state.bars[`${symbol}_${timeframe}`] || EMPTY_BARS);

  const formattedData = useMemo(() => {
    const data = bars.map(bar => ({
      time: (Math.floor(new Date(bar.timestamp).getTime() / 1000)) as UTCTimestamp,
      open: bar.open,
      high: bar.high,
      low: bar.low,
      close: bar.close,
    })).sort((a, b) => (a.time as number) - (b.time as number));
    
    return data;
  }, [bars]);

  useEffect(() => {
    if (!chartContainerRef.current) return;

    const chart = createChart(chartContainerRef.current, {
      layout: {
        background: { type: ColorType.Solid, color: '#0a0e1a' },
        textColor: '#c0c8d8',
      },
      grid: {
        vertLines: { color: '#1a2035' },
        horzLines: { color: '#1a2035' },
      },
      crosshair: {
        mode: CrosshairMode.Normal,
      },
      timeScale: {
        borderColor: '#1a2035',
        timeVisible: true,
        secondsVisible: false,
        shiftVisibleRangeOnNewBar: true,
      },
      rightPriceScale: {
        borderColor: '#1a2035',
        autoScale: true,
      },
    });

    const series = chart.addSeries(CandlestickSeries, {
      upColor: '#00d4a8',
      downColor: '#ff4757',
      borderUpColor: '#00d4a8',
      borderDownColor: '#ff4757',
      wickUpColor: '#00d4a8',
      wickDownColor: '#ff4757',
    });

    chartRef.current = chart;
    seriesRef.current = series;

    const handleResize = () => {
      chart.applyOptions({ width: chartContainerRef.current?.clientWidth });
    };

    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      chart.remove();
    };
  }, []);

  // Sync data to the series
  useEffect(() => {
    if (!seriesRef.current || !chartRef.current) return;

    const newKey = `${symbol}_${timeframe}`;
    const isNewKey = newKey !== currentKeyRef.current;
    
    // If symbol or timeframe changed, OR if we had no data and now we do (initial load)
    if (isNewKey || (lastTimestampRef.current === 0 && formattedData.length > 0)) {
      if (formattedData.length > 0) {
        seriesRef.current.setData(formattedData);
        lastTimestampRef.current = formattedData[formattedData.length - 1].time as number;
        chartRef.current.timeScale().fitContent();
      } else if (isNewKey) {
        seriesRef.current.setData([]);
        lastTimestampRef.current = 0;
      }
      currentKeyRef.current = newKey;
    } else if (formattedData.length > 0) {
      // Incremental update (e.g. new real-time bar)
      const lastBar = formattedData[formattedData.length - 1];
      const lastBarTime = lastBar.time as number;
      
      if (lastBarTime >= lastTimestampRef.current) {
        seriesRef.current.update(lastBar);
        lastTimestampRef.current = lastBarTime;
      }
    }
  }, [formattedData, symbol, timeframe]);

  return <div ref={chartContainerRef} className="w-full h-full" />;
};

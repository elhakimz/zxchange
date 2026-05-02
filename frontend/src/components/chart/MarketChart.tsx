import React, { useEffect, useRef, useMemo } from 'react';
import { 
  createChart, 
  ColorType, 
  CrosshairMode, 
  CandlestickSeries, 
  LineSeries, 
  AreaSeries, 
  BarSeries, 
  BaselineSeries 
} from 'lightweight-charts';
import type { IChartApi, ISeriesApi, UTCTimestamp, SeriesType } from 'lightweight-charts';
import { useMarketStore, EMPTY_BARS } from '../../store/market.store';
import type { Timeframe } from '../../types/market.types';

interface MarketChartProps {
  symbol: string;
  timeframe: Timeframe;
}

export const MarketChart: React.FC<MarketChartProps> = ({ symbol, timeframe }) => {
  const chartContainerRef = useRef<HTMLDivElement>(null);
  const chartRef = useRef<IChartApi | null>(null);
  const seriesRef = useRef<ISeriesApi<SeriesType> | null>(null);
  const lastTimestampRef = useRef<number>(0);
  
  const currentKeyRef = useRef<string>('');
  const chartType = useMarketStore((state) => state.selectedChartType);
  const bars = useMarketStore((state) => state.bars[`${symbol}_${timeframe}`] || EMPTY_BARS);

  const formattedData = useMemo(() => {
    return bars.map(bar => {
      const time = (Math.floor(new Date(bar.timestamp).getTime() / 1000)) as UTCTimestamp;
      if (chartType === 'line' || chartType === 'area' || chartType === 'baseline') {
        return { time, value: bar.close };
      }
      return {
        time,
        open: bar.open,
        high: bar.high,
        low: bar.low,
        close: bar.close,
      };
    }).sort((a, b) => (a.time as number) - (b.time as number));
  }, [bars, chartType]);

  // Initial Chart Setup
  useEffect(() => {
    if (!chartContainerRef.current) return;

    const chart = createChart(chartContainerRef.current, {
      width: chartContainerRef.current.clientWidth,
      height: chartContainerRef.current.clientHeight || 400,
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

    chartRef.current = chart;

    const resizeObserver = new ResizeObserver(entries => {
      if (entries.length === 0 || !chartContainerRef.current) return;
      const { width, height } = entries[0].contentRect;
      chart.applyOptions({ width, height });
    });

    resizeObserver.observe(chartContainerRef.current);

    return () => {
      resizeObserver.disconnect();
      chart.remove();
      chartRef.current = null;
      seriesRef.current = null;
    };
  }, []);

  // Series Type Management
  useEffect(() => {
    if (!chartRef.current) return;

    // Remove existing series if any
    if (seriesRef.current) {
      chartRef.current.removeSeries(seriesRef.current);
    }

    // Add new series based on type
    let series: ISeriesApi<SeriesType>;
    
    switch (chartType) {
      case 'line':
        series = chartRef.current.addSeries(LineSeries, {
          color: '#3b82f6',
          lineWidth: 2,
        });
        break;
      case 'area':
        series = chartRef.current.addSeries(AreaSeries, {
          lineColor: '#3b82f6',
          topColor: 'rgba(59, 130, 246, 0.4)',
          bottomColor: 'rgba(59, 130, 246, 0.0)',
          lineWidth: 2,
        });
        break;
      case 'bar':
        series = chartRef.current.addSeries(BarSeries, {
          upColor: '#00d4a8',
          downColor: '#ff4757',
        });
        break;
      case 'baseline':
        series = chartRef.current.addSeries(BaselineSeries, {
          baseValue: { type: 'price', price: formattedData.length > 0 ? formattedData[0].value : 0 },
          topLineColor: '#00d4a8',
          bottomLineColor: '#ff4757',
          topFillColor1: 'rgba(0, 212, 168, 0.28)',
          topFillColor2: 'rgba(0, 212, 168, 0.05)',
          bottomFillColor1: 'rgba(255, 71, 87, 0.05)',
          bottomFillColor2: 'rgba(255, 71, 87, 0.28)',
        });
        break;
      case 'candle':
      default:
        series = chartRef.current.addSeries(CandlestickSeries, {
          upColor: '#00d4a8',
          downColor: '#ff4757',
          borderUpColor: '#00d4a8',
          borderDownColor: '#ff4757',
          wickUpColor: '#00d4a8',
          wickDownColor: '#ff4757',
        });
        break;
    }

    seriesRef.current = series;
    
    // Reset timestamp tracker when series changes to force full data reload
    lastTimestampRef.current = 0;
  }, [chartType]);

  // Sync data to the series
  useEffect(() => {
    if (!seriesRef.current || !chartRef.current) return;

    const newKey = `${symbol}_${timeframe}_${chartType}`;
    const isNewKey = newKey !== currentKeyRef.current;
    
    if (isNewKey || (lastTimestampRef.current === 0 && formattedData.length > 0)) {
      if (formattedData.length > 0) {
        seriesRef.current.setData(formattedData as any);
        lastTimestampRef.current = formattedData[formattedData.length - 1].time as number;
        if (isNewKey) {
            chartRef.current.timeScale().fitContent();
        }
      } else if (isNewKey) {
        seriesRef.current.setData([]);
        lastTimestampRef.current = 0;
      }
      currentKeyRef.current = newKey;
    } else if (formattedData.length > 0) {
      const lastBar = formattedData[formattedData.length - 1];
      const lastBarTime = lastBar.time as number;
      
      if (lastBarTime >= lastTimestampRef.current) {
        seriesRef.current.update(lastBar as any);
        lastTimestampRef.current = lastBarTime;
      }
    }
  }, [formattedData, symbol, timeframe, chartType]);

  return <div ref={chartContainerRef} className="w-full h-full" />;
};

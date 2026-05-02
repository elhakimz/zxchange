import React from 'react';
import { useMarketStore } from '../../store/market.store';
import { TrendingUp, TrendingDown, X } from 'lucide-react';

interface WatchlistRowProps {
  symbol: string;
  onSelect: (symbol: string) => void;
  onRemove: (symbol: string) => void;
  isSelected: boolean;
}

export const WatchlistRow: React.FC<WatchlistRowProps> = ({ 
  symbol, 
  onSelect, 
  onRemove,
  isSelected 
}) => {
  const quote = useMarketStore((state) => state.quotes[symbol]);
  
  const formatCurrency = (value?: number) => {
    if (value === undefined) return '---';
    return new Intl.NumberFormat('en-US', { 
      style: 'currency', 
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(value);
  };

  const change = quote ? quote.askPrice - quote.bidPrice : 0; // Placeholder for real change
  const isUp = change >= 0;

  return (
    <div 
      onClick={() => onSelect(symbol)}
      className={`
        flex justify-between items-center p-2 rounded border transition-all cursor-pointer group relative
        ${isSelected 
          ? 'bg-brand/10 border-brand shadow-[0_0_8px_rgba(0,212,168,0.2)]' 
          : 'bg-bg-surface border-bg-border hover:border-text-muted'}
      `}
    >
      <div className="flex flex-col">
        <span className={`font-bold ${isSelected ? 'text-brand' : 'text-text-primary'}`}>{symbol}</span>
        <span className="text-[9px] text-text-muted uppercase tracking-tighter">IEX</span>
      </div>

      <div className="flex flex-col items-end mr-4">
        <span className={`font-mono text-xs ${isUp ? 'text-bull' : 'text-bear'}`}>
          {formatCurrency(quote?.askPrice)}
        </span>
        <div className={`flex items-center gap-0.5 text-[9px] ${isUp ? 'text-bull' : 'text-bear'}`}>
          {isUp ? <TrendingUp size={10} /> : <TrendingDown size={10} />}
          <span className="font-mono">
            {isUp ? '+' : ''}0.00%
          </span>
        </div>
      </div>

      <button
        onClick={(e) => {
          e.stopPropagation();
          onRemove(symbol);
        }}
        className="absolute right-1 top-1/2 -translate-y-1/2 p-1 text-text-muted hover:text-bear opacity-0 group-hover:opacity-100 transition-opacity"
      >
        <X size={12} />
      </button>
    </div>
  );
};

import React, { useState } from 'react';
import { Panel } from '../shared/Panel';
import { useWatchlists, useAddSymbol, useRemoveSymbol, useCreateWatchlist } from '../../api/watchlist.api';
import { WatchlistRow } from './WatchlistRow';
import { SymbolSearch } from './SymbolSearch';
import { useMarketStore } from '../../store/market.store';
import { Plus, ListFilter } from 'lucide-react';

export const WatchlistPanel: React.FC = () => {
  const { data: watchlists, isLoading } = useWatchlists();
  const addSymbolMutation = useAddSymbol();
  const removeSymbolMutation = useRemoveSymbol();
  const createWatchlistMutation = useCreateWatchlist();
  
  const selectedSymbol = useMarketStore((state) => state.selectedSymbol);
  const setSelectedSymbol = useMarketStore((state) => state.setSelectedSymbol);

  const [activeWatchlistId, setActiveWatchlistId] = useState<number | null>(null);

  const watchlistsData = watchlists || [];
  const currentActiveId = activeWatchlistId ?? (watchlistsData.length > 0 ? watchlistsData[0].id : null);
  const activeWatchlist = watchlistsData.find(w => w.id === currentActiveId);

  const handleAddSymbol = (symbol: string) => {
    if (currentActiveId) {
      addSymbolMutation.mutate({ id: currentActiveId, symbol });
    }
  };

  const handleRemoveSymbol = (symbol: string) => {
    if (currentActiveId) {
      removeSymbolMutation.mutate({ id: currentActiveId, symbol });
    }
  };

  const handleCreateWatchlist = () => {
    const name = prompt('Watchlist Name:');
    if (name) {
      createWatchlistMutation.mutate(name);
    }
  };

  return (
    <Panel 
      title="Watchlists" 
      className="h-full flex flex-col"
      actions={
        <div className="flex gap-1">
          <button 
            onClick={handleCreateWatchlist}
            className="text-text-muted hover:text-text-primary p-0.5" 
            title="New Watchlist"
          >
            <Plus size={14} />
          </button>
          <button className="text-text-muted hover:text-text-primary p-0.5" title="Filter"><ListFilter size={14} /></button>
        </div>
      }
    >
      <div className="p-2 border-b border-bg-border bg-bg-void/30">
        <div className="flex gap-1 overflow-x-auto pb-1 scrollbar-hide">
          {watchlistsData.map((w) => (
            <button
              key={w.id}
              onClick={() => setActiveWatchlistId(w.id)}
              className={`
                px-2 py-1 text-[10px] font-bold rounded transition-colors whitespace-nowrap
                ${currentActiveId === w.id 
                  ? 'bg-brand text-bg-void' 
                  : 'bg-bg-surface text-text-muted hover:text-text-primary'}
              `}
            >
              {w.name.toUpperCase()}
            </button>
          ))}
        </div>
      </div>

      <div className="p-2">
        <SymbolSearch onSelect={handleAddSymbol} />
      </div>

      <div className="flex-1 overflow-y-auto p-2 pt-0 space-y-1.5 custom-scrollbar">
        {isLoading ? (
          <div className="flex items-center justify-center h-20 text-data-xs text-text-muted animate-pulse">
            Loading Watchlists...
          </div>
        ) : !activeWatchlist || activeWatchlist.symbols.length === 0 ? (
          <div className="text-[10px] text-text-muted italic border border-dashed border-bg-border p-8 text-center mt-2 rounded">
            Watchlist is empty.<br/>Add a symbol above to start.
          </div>
        ) : (
          activeWatchlist.symbols.map((s) => (
            <WatchlistRow
              key={s.id}
              symbol={s.symbol}
              isSelected={selectedSymbol === s.symbol}
              onSelect={setSelectedSymbol}
              onRemove={handleRemoveSymbol}
            />
          ))
        )}
      </div>
    </Panel>
  );
};

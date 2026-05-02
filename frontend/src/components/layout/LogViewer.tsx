import React, { useState, useEffect, useRef } from 'react';
import { Terminal, ChevronUp, ChevronDown, Maximize2, Minimize2, Trash2, X } from 'lucide-react';
import { useMarketStore } from '../../store/market.store';

export const LogViewer: React.FC = () => {
  const logs = useMarketStore((state) => state.logs);
  const clearLogs = useMarketStore((state) => state.clearLogs);
  const [viewState, setViewState] = useState<'hidden' | 'min' | 'normal' | 'max'>('min');
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    // No auto-scroll logic needed since we are prepending new logs to the top
  }, [logs]);

  if (viewState === 'hidden') {
    return (
      <button 
        onClick={() => setViewState('min')}
        className="fixed bottom-10 right-4 bg-bg-surface border border-bg-border p-2 rounded-full shadow-lg text-text-muted hover:text-brand transition-colors z-50"
      >
        <Terminal size={18} />
      </button>
    );
  }

  const getHeight = () => {
    switch (viewState) {
      case 'min': return 'h-8';
      case 'normal': return 'h-64';
      case 'max': return 'h-[80vh]';
      default: return 'h-8';
    }
  };

  return (
    <div className={`fixed bottom-7 left-0 right-0 bg-bg-surface border-t border-bg-border transition-all duration-300 z-40 flex flex-col ${getHeight()}`}>
      {/* Header */}
      <div className="h-8 flex items-center justify-between px-3 bg-bg-void/50 border-b border-bg-border shrink-0 select-none">
        <div className="flex items-center gap-2">
          <Terminal size={14} className="text-brand" />
          <span className="text-[10px] font-bold uppercase tracking-wider text-text-secondary">Backend Activity Log</span>
          <span className="text-[9px] px-1.5 py-0.5 bg-bg-void rounded border border-bg-border text-text-muted font-mono">
            {logs.length} EVENTS
          </span>
        </div>
        
        <div className="flex items-center gap-1">
          <button onClick={clearLogs} className="p-1 text-text-muted hover:text-bear transition-colors" title="Clear Logs">
            <Trash2 size={12} />
          </button>
          <div className="w-px h-3 bg-bg-border mx-1"></div>
          
          {viewState === 'min' ? (
            <button onClick={() => setViewState('normal')} className="p-1 text-text-muted hover:text-text-primary">
              <ChevronUp size={14} />
            </button>
          ) : (
            <>
              <button onClick={() => setViewState(viewState === 'max' ? 'normal' : 'max')} className="p-1 text-text-muted hover:text-text-primary">
                {viewState === 'max' ? <Minimize2 size={12} /> : <Maximize2 size={12} />}
              </button>
              <button onClick={() => setViewState('min')} className="p-1 text-text-muted hover:text-text-primary">
                <ChevronDown size={14} />
              </button>
            </>
          )}
          <button onClick={() => setViewState('hidden')} className="p-1 text-text-muted hover:text-bear ml-1" title="Hide Viewer">
            <X size={12} />
          </button>
        </div>
      </div>

      {/* Log Content */}
      {(viewState === 'normal' || viewState === 'max') && (
        <div ref={scrollRef} className="flex-1 overflow-auto p-2 font-mono text-[10px] space-y-0.5 bg-bg-void">
          {logs.length === 0 ? (
            <div className="h-full flex items-center justify-center text-text-muted italic">
              Waiting for backend activity...
            </div>
          ) : (
            logs.map((log, i) => {
              const isError = log.includes('[ERROR]');
              const isWarn = log.includes('[WARN]');
              const isInfo = log.includes('[INFO]');
              const isDebug = log.includes('[DEBUG]');
              
              return (
                <div key={i} className="whitespace-pre-wrap break-all border-b border-bg-border/30 pb-0.5 last:border-0">
                  <span className={
                    isError ? 'text-bear' : 
                    isWarn ? 'text-yellow-500' : 
                    isInfo ? 'text-brand' : 
                    isDebug ? 'text-text-muted' : ''
                  }>
                    {log}
                  </span>
                </div>
              );
            })
          )}
        </div>
      )}
    </div>
  );
};

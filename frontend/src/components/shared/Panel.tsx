import React from 'react';
import { Square, Copy, ChevronDown, ChevronUp } from 'lucide-react';

interface PanelProps {
  title: string;
  children: React.ReactNode;
  actions?: React.ReactNode;
  className?: string;
  onMinimize?: () => void;
  onMaximize?: () => void;
  onRestore?: () => void;
  isMinimized?: boolean;
  isMaximized?: boolean;
  style?: React.CSSProperties;
}

export const Panel: React.FC<PanelProps> = ({
  title,
  actions,
  className = '',
  children,
  onMinimize,
  onMaximize,
  onRestore,
  isMinimized = false,
  isMaximized = false,
  style
}) => {
  return (
    <div 
      className={`bg-bg-surface border border-bg-border rounded-sm flex flex-col overflow-hidden ${className} ${isMinimized ? 'h-8 min-h-[32px] flex-none' : ''}`}
      style={style}
    >
      <div className="h-8 min-h-[32px] bg-bg-surface border-b border-bg-border flex items-center px-3 justify-between select-none">
        <div className="flex items-center gap-2 overflow-hidden">
          {onMinimize && (
            <button onClick={isMinimized ? onRestore : onMinimize} className="text-text-muted hover:text-text-primary transition-colors">
              {isMinimized ? <ChevronUp size={12} /> : <ChevronDown size={12} />}
            </button>
          )}
          <h2 className="text-label text-text-muted uppercase tracking-wider font-semibold truncate">
            {title}
          </h2>
        </div>
        
        <div className="flex items-center gap-3">
          {actions && <div className="flex items-center gap-2">{actions}</div>}
          
          {(onMaximize || onRestore) && (
            <div className="flex items-center gap-1.5 border-l border-bg-border pl-2 ml-1">
              {!isMinimized && onMaximize && !isMaximized && (
                <button onClick={onMaximize} className="text-text-muted hover:text-text-primary transition-colors" title="Maximize">
                  <Square size={10} />
                </button>
              )}
              {isMaximized && onRestore && (
                <button onClick={onRestore} className="text-text-muted hover:text-text-primary transition-colors" title="Restore">
                  <Copy size={10} />
                </button>
              )}
            </div>
          )}
        </div>
      </div>
      {!isMinimized && (
        <div className="flex-1 flex flex-col overflow-auto">
          {children}
        </div>
      )}
    </div>
  );
};

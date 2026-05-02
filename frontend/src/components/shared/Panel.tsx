import React from 'react';

interface PanelProps {
  title: string;
  children: React.ReactNode;
  actions?: React.ReactNode;
  className?: string;
}

export const Panel: React.FC<PanelProps> = ({
  title,
  actions,
  className = '',
  children
}) => {
  return (
    <div className={`bg-bg-surface border border-bg-border rounded-sm flex flex-col overflow-hidden ${className}`}>
      <div className="h-8 min-h-[32px] bg-bg-surface border-b border-bg-border flex items-center px-3 justify-between">
        <h2 className="text-label text-text-muted uppercase tracking-wider font-semibold truncate">
          {title}
        </h2>
        {actions && <div className="flex items-center gap-2">{actions}</div>}
      </div>
      <div className="flex-1 overflow-auto">
        {children}
      </div>
    </div>
  );
};

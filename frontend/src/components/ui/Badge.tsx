import React from 'react';

interface BadgeProps {
  variant?: 'bull' | 'bear' | 'warn' | 'neutral' | 'brand';
  children: React.ReactNode;
  className?: string;
  pulse?: boolean;
}

export const Badge: React.FC<BadgeProps> = ({
  variant = 'neutral',
  pulse = false,
  className = '',
  children
}) => {
  const variants = {
    bull: 'bg-bull-bg text-bull border-bull',
    bear: 'bg-bear-bg text-bear border-bear',
    warn: 'bg-warn-bg text-warn border-warn',
    neutral: 'bg-bg-overlay text-text-secondary border-bg-border',
    brand: 'bg-brand/10 text-brand border-brand/50',
  };

  const pulseClass = pulse ? 'animate-pulse' : '';

  return (
    <div className={`inline-flex items-center gap-1 border text-[9px] font-mono font-bold px-1.5 py-0.5 rounded-sm tracking-widest uppercase ${variants[variant]} ${pulseClass} ${className}`}>
      {children}
    </div>
  );
};

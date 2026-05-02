import React from 'react';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
}

export const Input: React.FC<InputProps> = ({
  label,
  error,
  className = '',
  ...props
}) => {
  return (
    <div className="flex flex-col gap-1 w-full">
      {label && (
        <label className="text-[10px] text-text-muted uppercase tracking-wider font-medium">
          {label}
        </label>
      )}
      <input
        className={`bg-bg-elevated border border-bg-border text-text-primary h-8 px-2 text-data-sm font-mono rounded-sm outline-none focus:border-brand focus:shadow-[0_0_0_2px_var(--brand-glow)] transition-all placeholder:text-text-muted ${className}`}
        {...props}
      />
      {error && (
        <span className="text-[9px] text-bear mt-0.5">{error}</span>
      )}
    </div>
  );
};

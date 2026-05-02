import React from 'react';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost' | 'bull' | 'bear';
  size?: 'xs' | 'sm' | 'md' | 'lg';
  fullWidth?: boolean;
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  fullWidth = false,
  className = '',
  children,
  ...props
}) => {
  const baseStyles = 'inline-flex items-center justify-center font-bold transition-colors disabled:opacity-35 disabled:cursor-not-allowed rounded-sm uppercase tracking-wider';
  
  const variants = {
    primary: 'bg-brand text-bg-void hover:bg-brand/90',
    secondary: 'bg-transparent border border-bg-border text-text-secondary hover:border-brand hover:text-brand',
    danger: 'bg-transparent border border-bear text-bear hover:bg-bear-bg',
    ghost: 'bg-transparent text-text-muted hover:text-text-primary hover:bg-bg-overlay',
    bull: 'bg-bull text-bg-void hover:bg-bull/90',
    bear: 'bg-bear text-white hover:bg-bear/90',
  };

  const sizes = {
    xs: 'h-6 px-2 text-[9px]',
    sm: 'h-8 px-3 text-[10px]',
    md: 'h-10 px-4 text-xs',
    lg: 'h-12 px-6 text-sm',
  };

  const widthStyle = fullWidth ? 'w-full' : '';

  return (
    <button
      className={`${baseStyles} ${variants[variant]} ${sizes[size]} ${widthStyle} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
};

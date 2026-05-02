import React, { useState, useCallback, useEffect } from 'react';

interface ResizerProps {
  onResize: (delta: number) => void;
  direction?: 'horizontal' | 'vertical';
  className?: string;
}

export const Resizer: React.FC<ResizerProps> = ({ 
  onResize, 
  direction = 'vertical',
  className = ''
}) => {
  const [isDragging, setIsChartDragging] = useState(false);

  const handleMouseDown = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    setIsChartDragging(true);
  }, []);

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      if (!isDragging) return;
      onResize(direction === 'vertical' ? e.movementY : e.clientX);
    };

    const handleMouseUp = () => {
      setIsChartDragging(false);
    };

    if (isDragging) {
      window.addEventListener('mousemove', handleMouseMove);
      window.addEventListener('mouseup', handleMouseUp);
      document.body.style.cursor = direction === 'vertical' ? 'row-resize' : 'col-resize';
    }

    return () => {
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('mouseup', handleMouseUp);
      document.body.style.cursor = '';
    };
  }, [isDragging, onResize, direction]);

  return (
    <div
      onMouseDown={handleMouseDown}
      className={`
        ${direction === 'vertical' ? 'h-1.5 w-full cursor-row-resize' : 'w-1.5 h-full cursor-col-resize'}
        hover:bg-brand/30 transition-colors shrink-0
        ${isDragging ? 'bg-brand/50' : 'bg-transparent'}
        ${className}
      `}
    />
  );
};

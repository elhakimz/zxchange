import React, { useState } from 'react';
import { Search } from 'lucide-react';
import { Input } from '../ui/Input';

interface SymbolSearchProps {
  onSelect: (symbol: string) => void;
}

export const SymbolSearch: React.FC<SymbolSearchProps> = ({ onSelect }) => {
  const [query, setQuery] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (query.trim()) {
      onSelect(query.trim().toUpperCase());
      setQuery('');
    }
  };

  return (
    <form onSubmit={handleSubmit} className="relative group">
      <Input
        placeholder="Add symbol..."
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        className="pr-8"
      />
      <button 
        type="submit"
        className="absolute right-2 top-1/2 -translate-y-1/2 text-text-muted hover:text-brand transition-colors"
      >
        <Search size={14} />
      </button>
    </form>
  );
};

import axios from 'axios';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

export interface WatchlistSymbol {
  id: number;
  symbol: string;
  positionIndex: number;
  addedAt: string;
}

export interface Watchlist {
  id: number;
  name: string;
  createdAt: string;
  symbols: WatchlistSymbol[];
}

const API_BASE = '/api/watchlists';

export const watchlistApi = {
  getAll: async (): Promise<Watchlist[]> => {
    const { data } = await axios.get(API_BASE);
    return data;
  },
  create: async (name: string): Promise<Watchlist> => {
    const { data } = await axios.post(API_BASE, { name });
    return data;
  },
  addSymbol: async (id: number, symbol: string): Promise<Watchlist> => {
    const { data } = await axios.post(`${API_BASE}/${id}/symbols`, { symbol });
    return data;
  },
  removeSymbol: async (id: number, symbol: string): Promise<Watchlist> => {
    const { data } = await axios.delete(`${API_BASE}/${id}/symbols/${symbol}`);
    return data;
  },
  delete: async (id: number): Promise<void> => {
    await axios.delete(`${API_BASE}/${id}`);
  },
};

export const useWatchlists = () => {
  return useQuery({
    queryKey: ['watchlists'],
    queryFn: watchlistApi.getAll,
  });
};

export const useCreateWatchlist = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: watchlistApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['watchlists'] });
    },
  });
};

export const useAddSymbol = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, symbol }: { id: number; symbol: string }) =>
      watchlistApi.addSymbol(id, symbol),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['watchlists'] });
    },
  });
};

export const useRemoveSymbol = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, symbol }: { id: number; symbol: string }) =>
      watchlistApi.removeSymbol(id, symbol),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['watchlists'] });
    },
  });
};

import { useMutation, useQuery } from '@tanstack/react-query';
import client from './client';

export interface OrderRequest {
  symbol: string;
  qty: number;
  side: 'buy' | 'sell';
  type: 'market' | 'limit' | 'stop' | 'stop_limit';
  time_in_force?: 'day' | 'gtc' | 'ioc' | 'fok';
  limit_price?: number;
  stop_price?: number;
  client_order_id?: string;
}

export interface Order {
  id: string;
  client_order_id: string;
  symbol: string;
  side: string;
  type: string;
  time_in_force: string;
  qty: number;
  filled_qty: number;
  filled_at: string;
  limit_price: number;
  stop_price: number;
  status: string;
  created_at: string;
  updated_at: string;
}

export interface Position {
  symbol: string;
  qty: number;
  side: string;
  avg_entry_price: number;
  current_price: number;
  market_value: number;
  cost_basis: number;
  unrealized_pl: number;
  unrealized_plpc: number;
}

export const useOrders = () => {
  return useQuery<Order[]>({
    queryKey: ['orders'],
    queryFn: async () => {
      const { data } = await client.get<Order[]>('/orders');
      return data;
    },
  });
};

export const usePlaceOrder = () => {
  return useMutation({
    mutationFn: async (order: OrderRequest) => {
      const { data } = await client.post<Order>('/orders', order);
      return data;
    },
  });
};

export const useCancelOrder = () => {
  return useMutation({
    mutationFn: async (orderId: string) => {
      await client.delete(`/orders/${orderId}`);
    },
  });
};

export const usePositions = () => {
  return useQuery<Position[]>({
    queryKey: ['positions'],
    queryFn: async () => {
      const { data } = await client.get<Position[]>('/positions');
      return data;
    },
  });
};

export const useClosePosition = () => {
  return useMutation({
    mutationFn: async (symbol: string) => {
      await client.delete(`/positions/${symbol}`);
    },
  });
};
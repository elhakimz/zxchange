import { useQuery } from '@tanstack/react-query';
import client from './client';

export interface Account {
  id: string;
  accountNumber: string;
  status: string;
  currency: string;
  buyingPower: string;
  cash: string;
  portfolioValue: string;
  equity: string;
  longMarketValue: string;
  shortMarketValue: string;
  initialMargin: string;
  maintenanceMargin: string;
  lastEquity: string;
  daytopPnl?: string;
}

export const useAccount = () => {
  return useQuery<Account>({
    queryKey: ['account'],
    queryFn: async () => {
      const { data } = await client.get<Account>('/account');
      return data;
    },
    refetchInterval: 10000, // Refresh every 10 seconds
  });
};

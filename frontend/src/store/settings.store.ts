import { create } from 'zustand';

interface SettingsState {
  apiKey: string;
  apiSecret: string;
  isPaper: boolean;
  setApiKey: (key: string) => void;
  setApiSecret: (secret: string) => void;
  setIsPaper: (isPaper: boolean) => void;
}

export const useSettingsStore = create<SettingsState>((set) => ({
  apiKey: '',
  apiSecret: '',
  isPaper: true,
  setApiKey: (apiKey) => set({ apiKey }),
  setApiSecret: (apiSecret) => set({ apiSecret }),
  setIsPaper: (isPaper) => set({ isPaper }),
}));

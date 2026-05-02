import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useMarketStore } from '../store/market.store';

export const stompClient = new Client({
  webSocketFactory: () => new SockJS('/ws'),
  reconnectDelay: 2000,
  onConnect: () => {
    console.log('[STOMP] Connected');
    useMarketStore.getState().setConnected(true);
  },
  onDisconnect: () => {
    console.log('[STOMP] Disconnected');
    useMarketStore.getState().setConnected(false);
  },
});

export const connectStomp = () => {
  if (!stompClient.active) {
    stompClient.activate();
  }
};

export const disconnectStomp = () => {
  if (stompClient.active) {
    stompClient.deactivate();
  }
};

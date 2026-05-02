import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useMarketStore } from '../store/market.store';

export const stompClient = new Client({
  webSocketFactory: () => new SockJS('/ws'),
  reconnectDelay: 5000, // Slightly longer delay
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000,
  onConnect: () => {
    console.log('[STOMP] Connected');
    useMarketStore.getState().setConnected(true);
    useMarketStore.getState().setConnectionError(null);
  },
  onDisconnect: () => {
    console.log('[STOMP] Disconnected');
    useMarketStore.getState().setConnected(false);
  },
  onStompError: (frame) => {
    console.error('[STOMP] Protocol error:', frame.headers['message']);
    console.error('[STOMP] Details:', frame.body);
    useMarketStore.getState().setConnectionError('STOMP Protocol Error');
  },
  onWebSocketClose: () => {
    console.warn('[STOMP] WebSocket closed');
    useMarketStore.getState().setConnected(false);
  },
  onWebSocketError: (event) => {
    console.error('[STOMP] WebSocket error:', event);
    useMarketStore.getState().setConnectionError('WebSocket Connection Failed');
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

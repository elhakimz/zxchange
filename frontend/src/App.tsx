import { useState, useEffect } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AppShell } from './components/layout/AppShell';
import { connectStomp, disconnectStomp } from './ws/stompClient';
import { useMarketStore, EMPTY_BARS } from './store/market.store';
import { useMarketStream } from './ws/useMarketStream';
import { useChartUpdateScheduler } from './ws/useChartUpdateScheduler';
import { useBars } from './api/marketdata.api';
import { useOrders, usePlaceOrder, useCancelOrder, usePositions, useClosePosition } from './api/orders.api';
import type { Timeframe } from './types/market.types';
import { Panel } from './components/shared/Panel';
import { Button } from './components/ui/Button';
import { Input } from './components/ui/Input';
import { WatchlistPanel } from './components/watchlist/WatchlistPanel';
import { CandlestickChart } from './components/chart/CandlestickChart';
import { Maximize2, X, Loader2 } from 'lucide-react';

const queryClient = new QueryClient();

function AppContent() {
  const selectedSymbol = useMarketStore((state) => state.selectedSymbol);
  const selectedTimeframe = useMarketStore((state) => state.selectedTimeframe);
  const quote = useMarketStore((state) => state.quotes[selectedSymbol]);

  useMarketStream(selectedSymbol, selectedTimeframe);
  useChartUpdateScheduler(selectedSymbol, selectedTimeframe);
  const { isLoading: isBarsLoading } = useBars(selectedSymbol, selectedTimeframe);

  const [side, setSide] = useState<'buy' | 'sell'>('buy');
  const [orderType, setOrderType] = useState<'market' | 'limit' | 'stop' | 'stop_limit'>('limit');
  const [qty, setQty] = useState<string>('100');
  const [limitPrice, setLimitPrice] = useState<string>('');
  const [tif, _setTif] = useState<string>('day');

  const { data: orders = [], isLoading: isOrdersLoading } = useOrders();
  const { data: positions = [], isLoading: isPositionsLoading } = usePositions();
  const placeOrderMutation = usePlaceOrder();
  const cancelOrderMutation = useCancelOrder();
  const closePositionMutation = useClosePosition();

  const formatCurrency = (value?: number) => {
    if (value === undefined) return '---';
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);
  };

  const handlePlaceOrder = async () => {
    const orderQty = parseFloat(qty);
    if (isNaN(orderQty) || orderQty <= 0) return;
    const order = {
      symbol: selectedSymbol,
      qty: orderQty,
      side,
      type: orderType,
      time_in_force: tif as 'day' | 'gtc',
      limit_price: (orderType === 'limit' || orderType === 'stop_limit') ? parseFloat(limitPrice) : undefined,
    };
    try {
      await placeOrderMutation.mutateAsync(order);
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    } catch (e) {
      console.error('Order failed:', e);
    }
  };

  const handleCancelOrder = async (orderId: string) => {
    try {
      await cancelOrderMutation.mutateAsync(orderId);
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    } catch (e) {
      console.error('Cancel failed:', e);
    }
  };

  const handleClosePosition = async (posSymbol: string) => {
    try {
      await closePositionMutation.mutateAsync(posSymbol);
      queryClient.invalidateQueries({ queryKey: ['positions'] });
    } catch (e) {
      console.error('Close position failed:', e);
    }
  };

  const timeframes: { label: string; value: Timeframe }[] = [
    { label: '1s', value: '1Sec' }, { label: '30s', value: '30Sec' }, { label: '1m', value: '1Min' },
    { label: '15m', value: '15Min' }, { label: '30m', value: '30Min' }, { label: '1h', value: '1Hour' },
    { label: '12h', value: '12Hour' }, { label: '1d', value: '1Day' }, { label: '1w', value: '1Week' },
  ];
  const setSelectedTimeframe = useMarketStore((state) => state.setSelectedTimeframe);
  const bars = useMarketStore((state) => state.bars[`${selectedSymbol}_${selectedTimeframe}`] || EMPTY_BARS);
  const estCost = (parseFloat(qty) || 0) * (parseFloat(limitPrice) || quote?.askPrice || 0);

  const chartActions = (
    <div className="flex items-center gap-1 bg-bg-void/50 p-0.5 rounded border border-bg-border mr-2">
      {timeframes.map((tf) => (
        <button key={tf.value} onClick={() => setSelectedTimeframe(tf.value)}
          className={`px-1.5 py-0.5 text-[10px] font-mono rounded transition-colors ${
            selectedTimeframe === tf.value ? 'bg-brand text-bg-void font-bold' : 'text-text-muted hover:text-text-primary'
          }`}>
          {tf.label}
        </button>
      ))}
      <div className="w-px h-3 bg-bg-border mx-1"></div>
      <button className="text-text-muted hover:text-text-primary p-0.5"><Maximize2 size={12} /></button>
    </div>
  );

  return (
    <AppShell>
      <div className="p-3 h-full flex flex-col gap-3 overflow-hidden">
        <div className="grid grid-cols-12 gap-3 h-full overflow-hidden">
          <div className="col-span-2 h-full overflow-hidden">
            <WatchlistPanel />
          </div>

          <Panel title={`${selectedSymbol} — ${selectedTimeframe} — NASDAQ`} className="col-span-7" actions={chartActions}>
            {isBarsLoading ? (
              <div className="flex-1 h-full flex items-center justify-center text-text-muted italic bg-bg-void/50">
                <Loader2 className="w-8 h-8 animate-spin" />
              </div>
            ) : (
              <div className="flex-1 h-full relative">
                {bars.length === 0 && (
                  <div className="absolute inset-0 flex items-center justify-center text-text-muted italic bg-bg-void/50 z-10">
                    <span className="text-data-xs uppercase tracking-widest font-mono">No data available</span>
                  </div>
                )}
                <CandlestickChart symbol={selectedSymbol} timeframe={selectedTimeframe} />
              </div>
            )}
          </Panel>

          <div className="col-span-3 flex flex-col gap-3 overflow-hidden">
            <Panel title="Order Ticket">
              <div className="p-4 space-y-4">
                <div className="flex gap-2">
                  <Button variant={side === 'buy' ? 'bull' : 'secondary'} size="sm" fullWidth onClick={() => setSide('buy')}>BUY</Button>
                  <Button variant={side === 'sell' ? 'bear' : 'secondary'} size="sm" fullWidth onClick={() => setSide('sell')}>SELL</Button>
                </div>
                <div className="space-y-3">
                  <Input label="Symbol" value={selectedSymbol} readOnly />
                  <Input label="Quantity" type="number" value={qty} onChange={(e) => setQty(e.target.value)} />
                  <div className="grid grid-cols-2 gap-2">
                    <select value={orderType} onChange={(e) => setOrderType(e.target.value as any)}
                      className="h-8 bg-bg-elevated border border-bg-border rounded px-2 text-xs text-text-primary">
                      <option value="market">Market</option>
                      <option value="limit">Limit</option>
                      <option value="stop">Stop</option>
                      <option value="stop_limit">Stop Limit</option>
                    </select>
                    {(orderType === 'limit' || orderType === 'stop_limit') && (
                      <Input label="Price" type="number" value={limitPrice} step="0.01" onChange={(e) => setLimitPrice(e.target.value)} />
                    )}
                  </div>
                </div>
                <div className="pt-2 border-t border-bg-border">
                  <div className="flex justify-between text-data-xs mb-1">
                    <span className="text-text-muted">Est. Cost</span>
                    <span className="text-text-primary font-mono">{formatCurrency(estCost)}</span>
                  </div>
                  <Button variant={side === 'buy' ? 'primary' : 'bear'} fullWidth className="mt-2"
                    onClick={handlePlaceOrder} disabled={placeOrderMutation.isPending}>
                    {placeOrderMutation.isPending ? 'Placing...' : `PLACE ${side.toUpperCase()} ORDER`}
                  </Button>
                </div>
              </div>
            </Panel>

            <Panel title="Open Orders" className="flex-1 overflow-auto">
              {isOrdersLoading ? (
                <div className="p-3 text-text-muted"><Loader2 className="w-4 h-4 animate-spin" /></div>
              ) : orders.length === 0 ? (
                <div className="p-3 text-data-xs text-text-muted italic">No open orders</div>
              ) : (
                <div className="space-y-2 p-2">
                  {orders.map((order) => (
                    <div key={order.id} className="flex items-center justify-between bg-bg-surface p-2 rounded border border-bg-border">
                      <div className="flex flex-col">
                        <span className="text-xs font-bold">{order.symbol}</span>
                        <span className="text-[10px] text-text-muted">{order.side} {order.type} {order.qty}</span>
                      </div>
                      <button onClick={() => handleCancelOrder(order.id)} className="text-bear hover:text-bear/80">
                        <X size={14} />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </Panel>

            <Panel title="Positions" className="flex-1 overflow-auto">
              {isPositionsLoading ? (
                <div className="p-3 text-text-muted"><Loader2 className="w-4 h-4 animate-spin" /></div>
              ) : positions.length === 0 ? (
                <div className="p-3 text-data-xs text-text-muted italic">No open positions</div>
              ) : (
                <div className="space-y-2 p-2">
                  {positions.map((pos) => (
                    <div key={pos.symbol} className="flex items-center justify-between bg-bg-surface p-2 rounded border border-bg-border">
                      <div className="flex flex-col">
                        <span className="text-xs font-bold">{pos.symbol}</span>
                        <span className="text-[10px] text-text-muted">{pos.qty} shares @ {pos.avg_entry_price}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className={`text-xs font-mono ${pos.unrealized_pl >= 0 ? 'text-bull' : 'text-bear'}`}>
                          {pos.unrealized_pl >= 0 ? '+' : ''}{formatCurrency(pos.unrealized_pl)}
                        </span>
                        <button onClick={() => handleClosePosition(pos.symbol)} className="text-bear hover:text-bear/80">
                          <X size={14} />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </Panel>
          </div>
        </div>
      </div>
    </AppShell>
  );
}

function App() {
  useEffect(() => {
    connectStomp();
    return () => disconnectStomp();
  }, []);

  return (
    <QueryClientProvider client={queryClient}>
      <AppContent />
    </QueryClientProvider>
  );
}

export default App;
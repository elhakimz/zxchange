import { useEffect, useState } from 'react';
import { Wifi } from 'lucide-react';
import { useMarketStore, EMPTY_BARS } from '../../store/market.store';

type Exchange = 'NYSE' | 'NASDAQ';

interface ExchangeHours {
  name: string;
  openHour: number;
  closeHour: number;
  preOpen?: number;
  afterHours?: number;
  timezone: string;
}

const EXCHANGES: Record<Exchange, ExchangeHours> = {
  NYSE: {
    name: 'NYSE',
    openHour: 570,
    closeHour: 960,
    preOpen: 240,
    afterHours: 1260,
    timezone: 'America/New_York',
  },
  NASDAQ: {
    name: 'NASDAQ',
    openHour: 570,
    closeHour: 960,
    preOpen: 240,
    afterHours: 1260,
    timezone: 'America/New_York',
  },
};

const getExchangeStatus = (exchange: Exchange): string => {
  const ex = EXCHANGES[exchange];
  const now = new Date();
  const et = new Date(now.toLocaleString('en-US', { timeZone: ex.timezone }));
  const day = et.getDay();
  const minute = et.getHours() * 60 + et.getMinutes();
  
  if (day === 0 || day === 6) return 'CLOSED';
  if (minute >= ex.openHour && minute < ex.closeHour) return 'OPEN';
  if (ex.preOpen && minute >= ex.preOpen && minute < ex.openHour) return 'PRE-MARKET';
  if (ex.afterHours && minute >= ex.afterHours) return 'AFTER-HOURS';
  return 'CLOSED';
};

const formatExchangeTime = (exchange: Exchange): string => {
  const ex = EXCHANGES[exchange];
  const now = new Date();
  const et = new Date(now.toLocaleString('en-US', { timeZone: ex.timezone }));
  return et.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false }) + ' ET';
};

export const StatusBar: React.FC = () => {
  const connected = useMarketStore((state) => state.connected);
  const selectedSymbol = useMarketStore((state) => state.selectedSymbol);
  const bars = useMarketStore((state) => state.bars[`${selectedSymbol}_1Min`] || EMPTY_BARS);
  const quote = useMarketStore((state) => state.quotes[selectedSymbol]);
  const [lastPrice, setLastPrice] = useState<number | null>(null);
  const [exchange] = useState<Exchange>('NASDAQ');

  useEffect(() => {
    if (bars.length > 0) {
      setLastPrice(bars[bars.length - 1].close);
    }
  }, [bars]);

  const displayPrice = quote?.askPrice || lastPrice;
  const priceStr = displayPrice ? '$' + displayPrice.toFixed(2) : '---';
  const status = getExchangeStatus(exchange);
  const exchangeTime = formatExchangeTime(exchange);

  return (
    <div className="h-7 bg-bg-void border-t border-bg-border flex items-center px-3 justify-between text-data-xs text-text-secondary">
      <div className="flex items-center gap-4">
        <div className="flex items-center gap-1.5">
          <Wifi size={12} className={connected ? 'text-bull' : 'text-bear'} />
          <span>{connected ? 'STOMP CONNECTED' : 'STOMP OFFLINE'}</span>
        </div>
        <div className="h-3 w-px bg-bg-border mx-1"></div>
        <div>DATA: IEX (FREE)</div>
      </div>

      <div className="flex items-center gap-4 overflow-hidden">
        {displayPrice ? (
          <div className="flex items-center gap-2">
            <span className="font-bold text-text-primary">{selectedSymbol}</span>
            <span className={quote ? 'text-bull' : 'text-text-secondary'}>{priceStr}</span>
          </div>
        ) : null}
      </div>

      <div className="flex items-center gap-4">
        <div className="flex items-center gap-1.5">
          <span className={`w-1.5 h-1.5 rounded-full ${
            status === 'OPEN' ? 'bg-bull' : 
            status === 'PRE-MARKET' || status === 'AFTER-HOURS' ? 'bg-warn' : 
            'bg-text-muted'
          }`}></span>
          <span>{exchange}</span>
          <span className="text-text-muted">|</span>
          <span>{status}</span>
        </div>
        <div className="text-[10px] font-mono">{exchangeTime}</div>
      </div>
    </div>
  );
};
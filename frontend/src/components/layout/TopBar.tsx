import React from 'react';
import { useSettingsStore } from '../../store/settings.store';
import { useMarketStore } from '../../store/market.store';
import { useAccount } from '../../api/account.api';
import { Shield, ShieldAlert, Settings as SettingsIcon } from 'lucide-react';
import { Badge } from '../ui/Badge';

export const TopBar: React.FC = () => {
  const { isPaper } = useSettingsStore();
  const { data: initialAccount, isLoading: isInitialLoading } = useAccount();
  const account = useMarketStore((state) => state.account);
  const updateAccount = useMarketStore((state) => state.updateAccount);

  React.useEffect(() => {
    if (initialAccount && !account) {
      updateAccount(initialAccount);
    }
  }, [initialAccount, account, updateAccount]);

  const currentAccount = account || initialAccount;
  const isLoading = isInitialLoading && !account;

  const formatCurrency = (value?: string | number) => {
    if (value === undefined) return '---';
    const num = typeof value === 'string' ? parseFloat(value) : value;
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(num);
  };

  const calculateDayPnL = () => {
    if (!currentAccount || currentAccount.equity === undefined || currentAccount.lastEquity === undefined) {
      return { value: 0, percent: 0 };
    }
    const equity = parseFloat(currentAccount.equity);
    const lastEquity = parseFloat(currentAccount.lastEquity);
    
    if (isNaN(equity) || isNaN(lastEquity)) {
      return { value: 0, percent: 0 };
    }

    const diff = equity - lastEquity;
    const pct = lastEquity !== 0 ? (diff / lastEquity) * 100 : 0;
    return { value: diff, percent: pct };
  };

  const pnl = calculateDayPnL();

  return (
    <div className="h-10 bg-bg-void border-b border-bg-border flex items-center px-4 justify-between shrink-0">
      <div className="flex items-center gap-4">
        <span className="font-bold text-brand tracking-tighter text-lg">ZXChange</span>
        {isPaper ? (
          <Badge variant="warn">
            <Shield size={10} /> PAPER
          </Badge>
        ) : (
          <Badge variant="bear" pulse>
            <ShieldAlert size={10} /> LIVE ⚠
          </Badge>
        )}
      </div>

      <div className="flex items-center gap-6 text-data-sm font-mono">
        <div className="flex flex-col">
          <span className="text-[9px] text-text-muted uppercase tracking-wider leading-none">Equity</span>
          <span className="text-text-primary leading-tight">
            {isLoading ? 'Loading...' : formatCurrency(currentAccount?.equity)}
          </span>
        </div>
        <div className="flex flex-col">
          <span className="text-[9px] text-text-muted uppercase tracking-wider leading-none">Day P&L</span>
          <span className={`${pnl.value >= 0 ? 'text-bull' : 'text-bear'} leading-tight`}>
            {isLoading ? '---' : `${pnl.value >= 0 ? '+' : ''}${formatCurrency(pnl.value)} (${pnl.percent.toFixed(2)}%)`}
          </span>
        </div>
        <div className="flex flex-col">
          <span className="text-[9px] text-text-muted uppercase tracking-wider leading-none">Buying Power</span>
          <span className="text-text-primary leading-tight">
            {isLoading ? 'Loading...' : formatCurrency(currentAccount?.buyingPower)}
          </span>
        </div>
      </div>

      <div className="flex items-center gap-3">
        <div className="flex items-center gap-1.5 text-data-xs text-text-secondary bg-bg-surface px-2 py-1 rounded-sm border border-bg-border">
          <span className="w-1.5 h-1.5 rounded-full bg-bull shadow-[0_0_5px_var(--signal-bull)] animate-pulse-dot"></span>
          <span className="font-mono">OPEN 14:35 EST</span>
        </div>
        <button className="text-text-muted hover:text-brand transition-colors p-1">
          <SettingsIcon size={16} />
        </button>
      </div>
    </div>
  );
};

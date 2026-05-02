# Lessons Learned

## Build & Environment
- **TypeScript Strictness:** Always check for unused imports and missing type imports in React components when refactoring or adding new features.
- **Backend Tests:** The project initially had no backend tests. Future tasks should prioritize adding coverage for critical paths like external service integrations and encryption.

## Real-time Data & UI
- **Chart Schedulers:** When building chart update schedulers (like `useChartUpdateScheduler`), always use `refs` for high-frequency data (quotes). Restarting intervals on every state update (quote arrival) can prevent the interval from ever firing if the data frequency is higher than the interval time.
- **Finnhub Limitations:** Be aware of Finnhub's free tier restrictions (403 on US stock candles). Implement mock data generators to keep the UI functional during development.
- **Resize Handling:** Always use `ResizeObserver` for charting components instead of standard window resize listeners to ensure correct layout within nested panels.

# Project Changes & Deviations from Base Plan

This document tracks major architectural and feature changes made during development that deviate from or enhance the original `ZXChange_PRD.md` and `ZXChange_TDD.md`.

## 1. Provider Migration (Alpaca â†’ Finnhub)
- **Reason:** To enable standalone operation and remove dependency on a specific brokerage account for initial development and testing.
- **Changes:**
    - Replaced Alpaca REST and WebSocket clients with **Finnhub.io** integrations.
    - Updated `application.yml` and `.env` to manage Finnhub API keys.
    - Renamed all backend Alpaca-prefixed services and clients to `FinnhubService`, `FinnhubMarketDataWsClient`, etc.

## 2. Local Mock Trading Engine
- **Reason:** Since Finnhub is a data-only provider, a local execution environment was needed to maintain the platform's trading features.
- **Changes:**
    - Implemented a **Mock Trading Engine** using the local SQLite database.
    - Added `MockAccountEntity` and `MockPositionEntity` to track cash, starting equity, and holdings locally.
    - Refactored `OrderService` to "fill" market orders immediately using real-time quotes from Finnhub.
    - Refactored `PositionService` and `PortfolioService` to calculate market value and unrealized P&L locally.
    - Decoupled `AccountController` from external broker APIs; it now serves data from the local simulation.

## 3. Data Resilience & Quality
- **Standardized Timestamps:** Converted all incoming market data (REST and WebSocket) to **ISO-8601** format in the backend. This ensures consistent parsing in the React frontend and prevents charting/status-bar rendering errors.
- **Mock Bar Fallback:** Implemented a **Mock Data Generator** in the backend. If the Finnhub Free Tier returns a `403 Forbidden` (common for US Stock historical candles), the system automatically generates realistic simulated history ending at the current market price. This keeps the UI functional and visually accurate for all symbols.
- **Crypto Support:** Added automatic routing for crypto symbols (e.g., `BINANCE:BTCUSDT`) to Finnhub's specific `/crypto/candle` endpoints.

## 4. UI/UX Enhancements
- **Chart Maximization:** Added a "Maximize" toggle to the Chart Panel. When active, it hides the Watchlist and Order panels, expanding the chart to `col-span-12` for a focused analysis mode.
- **Dynamic Resize Handling:** Integrated `ResizeObserver` into the `CandlestickChart` component to ensure the charting engine instantly and correctly recalculates its dimensions during window or panel resizing.
- **Real-time Fix:** Refactored the chart update scheduler to use `refs` for price updates, preventing high-frequency data streams (like Bitcoin) from freezing the chart rendering interval.

## 5. Engineering Standards
- **Mock Matching Engine:** Implemented a scheduled background task in `OrderService` that polls for open orders and executes them when real-time price conditions (Limit/Stop) are met. This provides a complete end-to-end trading simulation.
- **Java 25 Compatibility:** Updated `pom.xml` with the `-Dnet.bytebuddy.experimental=true` flag to support Mockito dynamic agent loading on modern JDKs.
- **Testing:** Added 7 backend unit tests for `FinnhubService` and `OrderService` using `MockWebServer` and Mockito. Verified the full frontend Playwright E2E suite with 100% pass rate.
- **Documentation:** Created a comprehensive root `README.md` and updated `GEMINI.md` to reflect the new "Broker-Agnostic" architecture.

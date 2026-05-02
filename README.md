# ZXChange

A professional-grade desktop trading platform designed for algorithmic traders and retail investors. This application provides real-time market data visualization and a local simulation environment for trading.

## 🚀 Project Overview

ZXChange follows a "Local Bridge" architecture:
- **Backend (Spring Boot):** Acts as a bridge between high-frequency market data providers (Finnhub.io) and the UI. It manages local persistence in SQLite and handles a **Mock Trading Engine** for risk-free simulation.
- **Frontend (React):** A data-dense, mission-control inspired UI built with Tailwind CSS and Lightweight Charts.

## ✨ Key Features

- **Real-time Market Data:** Seamless integration with Finnhub.io for stock and crypto data via WebSockets and REST.
- **Mock Trading Service:** Execute market, limit, and stop orders locally against real-time prices without an external broker account.
- **Dynamic Charting:** Interactive candlestick charts with real-time updates and multiple timeframe support (1s to 1w).
- **Portfolio Management:** Persistent local tracking of cash balance, positions, and equity.
- **Professional UI:** High-density, theme-able dashboard inspired by Bloomberg Terminal.

## 🛠 Tech Stack

### Backend
- **Java 21**
- **Spring Boot 3.3.x**
- **Spring Data JPA**
- **SQLite**
- **OkHttp / STOMP**

### Frontend
- **React 18 / Vite 5**
- **TypeScript**
- **Tailwind CSS**
- **Zustand** (State Management)
- **TanStack Query** (Data Fetching)
- **Lightweight Charts** (TradingView)

## 🚦 Getting Started

### Prerequisites
- JDK 21+
- Node.js 20+
- Maven 3.9+
- [Finnhub.io](https://finnhub.io/) API Key

### Configuration
Create a `.env` file in the project root:
```env
FINNHUB_API_KEY=your_key_here
```

### Running the App

#### 1. Start the Backend
```powershell
cd backend
mvn spring-boot:run
```

#### 2. Start the Frontend
```powershell
cd frontend
npm install
npm run dev
```
Open [http://localhost:5173](http://localhost:5173) in your browser.

## 🧪 Testing

### Backend
```powershell
cd backend
mvn test
```

### Frontend (E2E)
```powershell
cd frontend
npx playwright test
```

## 📄 License
This project is licensed under the MIT License.

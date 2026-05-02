# GEMINI.md - ZXChange Project Instructions


** LLM GUIDELINES **
Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes. **

** currently you are working in windows 11 powershell **


** PROJECT GUIDELINES **
This file provides architectural context and development guidelines for **ZXChange**, a professional-grade desktop trading platform.

## Project Overview
ZXChange is a desktop application designed for algorithmic traders and sophisticated retail investors. It integrates with Alpaca Markets to provide real-time market data, portfolio analytics, and order execution.

### Tech Stack
- **Backend:** Java 21, Spring Boot 3.3.x, Spring Data JPA, SQLite JDBC.
- **Frontend:** React 18, Vite 5, TypeScript, Tailwind CSS, Zustand, TanStack Query.
- **Communication:** HTTP REST for management; STOMP over WebSocket for real-time data streaming.
- **Charting:** Lightweight Charts (TradingView).
- **Database:** Local SQLite for persistence (watchlists, settings, trade history).

## Architecture
The system follows a "Local Bridge" architecture:
1.  **Spring Boot Backend (Port 8080):** Connects to Alpaca's REST and WebSocket APIs. It normalizes data, manages local persistence in SQLite, and broadcasts updates to the frontend via a local STOMP broker.
2.  **React Frontend (Port 5173):** A dense, data-rich UI inspired by Bloomberg Terminal and NASA Mission Control. It consumes the local backend's API.
3.  **Security:** Alpaca API keys are stored encrypted on the backend and never exposed to the frontend. Access is restricted to `localhost`.

## Building and Running

### Development Mode
1.  **Backend:**
    ```powershell
    cd backend
    mvn spring-boot:run
    ```
2.  **Frontend:**
    ```powershell
    cd frontend
    npm install
    npm run dev
    ```

### Production Build
1.  Build the frontend: `cd frontend; npm run build`
2.  Copy `dist` contents to `backend/src/main/resources/static/`.
3.  Build the backend JAR: `cd backend; mvn clean package`
4.  Run: `java -jar target/zxchange-1.0.0.jar`

## Development Conventions
- **Surgical Changes:** Adhere strictly to the guidelines in `GUIDE.md`. Touch only what is necessary, match existing style, and prioritize simplicity.
- **Type Safety:** Use TypeScript for the frontend and Java 21 features (records, etc.) for the backend.
- **Real-time Data:** All market data (quotes, trades, bars) must flow through the backend WebSocket client and be broadcast via STOMP.
- **Styling:** Use Tailwind CSS for the UI. Refer to `app_plan/ZXChange_DESIGN.md` for the color palette and typography scale.
- **Persistence:** Use Spring Data JPA with the SQLite dialect for all local data.

## Key Documentation
Detailed specifications are located in the `app_plan/` directory:
- `ZXChange_PRD.md`: Feature requirements and product roadmap.
- `ZXChange_TDD.md`: Technical architecture and API contracts.
- `ZXChange_DESIGN.md`: Design system, color tokens, and layout specs.

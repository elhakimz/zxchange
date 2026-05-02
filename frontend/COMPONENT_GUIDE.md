# Frontend Component Methodology

For **ZXChange**, we follow a structured component architecture inspired by Atomic Design and Domain-Driven Design.

## Component Layers

### 1. UI Primitives (`src/components/ui`)
Highly reusable, low-level components (Atoms). They should be stateless and styled primarily via Tailwind tokens.
- **Examples:** `Button`, `Input`, `Badge`, `Skeleton`, `Tooltip`.
- **Rule:** No business logic or API calls.

### 2. Composite UI (`src/components/shared`)
Generic but complex UI patterns (Molecules).
- **Examples:** `DataGrid`, `Panel`, `Modal`, `ScrollArea`.
- **Rule:** Can manage internal UI state (e.g., "is open") but remains domain-agnostic.

### 3. Feature Organisms (`src/components/[feature]`)
Domain-specific components tied to a specific feature area.
- **Examples:** `WatchlistRow`, `OrderTicket`, `CandlestickChart`, `PositionTable`.
- **Rule:** Uses Zustand stores or TanStack Query hooks. Highly specialized.

### 4. Layouts (`src/components/layout`)
Structural components that define the shell of the application.
- **Examples:** `AppShell`, `TopBar`, `StatusBar`.

### 5. Pages (`src/pages`)
Root containers for routes. They compose Organisms and Layouts.

---

## Development Workflow

1.  **Define Props:** Use TypeScript interfaces for all props.
2.  **Style with Tokens:** Use the custom Tailwind tokens (e.g., `text-bull`, `bg-bg-surface`).
3.  **Monospaced Data:** All numeric/price values MUST use `font-mono`.
4.  **Composition over Props:** Prefer children composition for flexible containers.
5.  **Storybook (Optional):** Consider for complex UI primitives.

## Component Checklist
- [ ] Type-safe props
- [ ] Design token compliance
- [ ] Keyboard accessibility
- [ ] ARIA labels for data/status
- [ ] Responsive/Density aware (Compact by default)

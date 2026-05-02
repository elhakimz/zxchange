import type { Config } from 'tailwindcss'

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        // Backgrounds
        'bg-void':      '#05080f',
        'bg-base':      '#0a0e1a',
        'bg-surface':   '#0f1629',
        'bg-elevated':  '#151d35',
        'bg-overlay':   '#1a2440',
        'bg-border':    '#1e2d4a',
        // Text
        'text-primary':   '#e8edf5',
        'text-secondary': '#8a9bb8',
        'text-muted':     '#4a5a78',
        'text-accent':    '#4fc3f7',
        // Signals
        'bull':         '#00d4a8',
        'bull-dim':     '#00896c',
        'bull-bg':      '#002a22',
        'bear':         '#ff4757',
        'bear-dim':     '#c0303e',
        'bear-bg':      '#2a0008',
        'warn':         '#ffc107',
        'warn-bg':      '#2a1f00',
        // Brand
        'brand':        '#4fc3f7',
        'brand-purple': '#7c5cbf',
      },
      fontFamily: {
        ui:   ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'Consolas', 'monospace'],
      },
      fontSize: {
        'data-lg': ['20px', { lineHeight: '1.2', fontWeight: '600' }],
        'data-md': ['14px', { lineHeight: '1.4', fontWeight: '500' }],
        'data-sm': ['12px', { lineHeight: '1.4', fontWeight: '400' }],
        'data-xs': ['11px', { lineHeight: '1.3', fontWeight: '400' }],
        'label':   ['10px', { lineHeight: '1', fontWeight: '500',
                              letterSpacing: '0.08em' }],
      },
      borderRadius: {
        DEFAULT: '2px',
        sm: '1px',
        md: '4px',
      },
      animation: {
        'flash-bull': 'flash-bull 300ms ease-out forwards',
        'flash-bear': 'flash-bear 300ms ease-out forwards',
        'pulse-live': 'pulse-live 2s ease-in-out infinite',
        'pulse-dot':  'pulse-dot 1.5s ease-in-out infinite',
      },
      keyframes: {
        'flash-bull': {
          '0%':   { backgroundColor: '#002a22' },
          '100%': { backgroundColor: 'transparent' },
        },
        'flash-bear': {
          '0%':   { backgroundColor: '#2a0008' },
          '100%': { backgroundColor: 'transparent' },
        },
        'pulse-live': {
          '0%, 100%': { opacity: '1' },
          '50%':      { opacity: '0.5' },
        },
        'pulse-dot': {
          '0%, 100%': { boxShadow: '0 0 0 0 rgba(0, 212, 168, 0.4)' },
          '70%':      { boxShadow: '0 0 0 6px rgba(0, 212, 168, 0)' },
        },
      },
    },
  },
  plugins: [],
} satisfies Config

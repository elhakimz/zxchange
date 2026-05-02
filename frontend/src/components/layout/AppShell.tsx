import React from 'react';
import { TopBar } from './TopBar';
import { StatusBar } from './StatusBar';

interface AppShellProps {
  children: React.ReactNode;
}

export const AppShell: React.FC<AppShellProps> = ({ children }) => {
  return (
    <div className="flex flex-col h-screen w-screen bg-bg-void overflow-hidden select-none">
      <TopBar />
      <main className="flex-1 overflow-hidden relative">
        {children}
      </main>
      <StatusBar />
    </div>
  );
};

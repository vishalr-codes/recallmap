import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { TopNavigation } from './TopNavigation';

export function AppShell() {
  const [mobileNavigationOpen, setMobileNavigationOpen] = useState(false);

  return (
    <div className="min-h-screen bg-[#080d0b] text-slate-100">
      <Sidebar mobileOpen={mobileNavigationOpen} onClose={() => setMobileNavigationOpen(false)} />
      <div className="min-h-screen lg:pl-[260px]">
        <TopNavigation onMenuClick={() => setMobileNavigationOpen(true)} />
        <main className="mx-auto w-full max-w-[1600px] px-4 py-6 sm:px-6 lg:px-8 lg:py-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

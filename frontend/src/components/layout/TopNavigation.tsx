import { useState } from 'react';
import { Bell, ChevronDown, Menu, Search, Settings, UserRound } from 'lucide-react';
import { Link, useLocation } from 'react-router-dom';
import { getPageForPath } from '../../app/navigation';
import { Button } from '../ui/Button';

type TopNavigationProps = {
  onMenuClick: () => void;
};

export function TopNavigation({ onMenuClick }: TopNavigationProps) {
  const [accountMenuOpen, setAccountMenuOpen] = useState(false);
  const [workspaceMenuOpen, setWorkspaceMenuOpen] = useState(false);
  const location = useLocation();
  const page = getPageForPath(location.pathname);
  const title = page?.title ?? 'Not found';

  return (
    <header className="sticky top-0 z-30 flex h-[72px] items-center justify-between border-b border-white/[0.07] bg-[#080d0b]/95 px-4 backdrop-blur-md sm:px-6 lg:px-8">
      <div className="flex min-w-0 items-center gap-3">
        <Button
          aria-label="Open navigation"
          className="px-2.5 lg:hidden"
          onClick={onMenuClick}
          variant="quiet"
        >
          <Menu className="h-5 w-5" />
        </Button>
        <div className="min-w-0">
          <nav aria-label="Breadcrumb" className="flex items-center gap-2 text-xs text-slate-500">
            <span className="hidden sm:inline">Workspace</span>
            <span aria-hidden="true" className="hidden sm:inline">/</span>
            <span aria-current="page" className="truncate text-slate-200">{title}</span>
          </nav>
          <p className="mt-0.5 truncate text-[11px] text-slate-500">Demo sandbox · sample data only</p>
        </div>
      </div>

      <div className="flex items-center gap-1 sm:gap-2">
        <Button aria-label="Search is not available yet" className="hidden sm:inline-flex" disabled variant="quiet">
          <Search className="h-4 w-4" />
          <span className="text-xs">Search</span>
        </Button>
        <div className="relative hidden sm:block">
          <Button
            aria-expanded={workspaceMenuOpen}
            aria-haspopup="menu"
            onClick={() => setWorkspaceMenuOpen((open) => !open)}
            className="gap-2"
          >
            <span className="h-1.5 w-1.5 rounded-full bg-amber-300" />
            <span className="text-xs">Sandbox</span>
            <ChevronDown className="h-3.5 w-3.5 text-slate-500" />
          </Button>
          {workspaceMenuOpen && (
            <div className="absolute right-0 mt-2 w-64 rounded-xl border border-white/10 bg-[#111916] p-3 shadow-xl" role="menu">
              <p className="text-xs font-medium text-slate-200">Demo sandbox</p>
              <p className="mt-1 text-xs leading-5 text-slate-500">
                Workspace switching is unavailable until authenticated workspace APIs are connected.
              </p>
            </div>
          )}
        </div>
        <Link
          aria-label="Notifications"
          className="inline-flex h-10 w-10 items-center justify-center rounded-xl text-slate-400 transition-colors hover:bg-white/[0.05] hover:text-white"
          to="/notifications"
        >
          <Bell className="h-[18px] w-[18px]" />
        </Link>
        <div className="relative">
          <Button
            aria-expanded={accountMenuOpen}
            aria-haspopup="menu"
            aria-label="Account menu"
            onClick={() => setAccountMenuOpen((open) => !open)}
            className="gap-2 px-2.5"
          >
            <span className="flex h-6 w-6 items-center justify-center rounded-lg bg-white/[0.08]">
              <UserRound className="h-3.5 w-3.5" />
            </span>
            <span className="hidden text-xs text-slate-300 md:inline">Account</span>
            <ChevronDown className="hidden h-3.5 w-3.5 text-slate-500 md:block" />
          </Button>
          {accountMenuOpen && (
            <div
              className="absolute right-0 mt-2 w-52 rounded-xl border border-white/10 bg-[#111916] p-1.5 shadow-xl"
              role="menu"
            >
              <p className="px-3 py-2 text-xs leading-5 text-slate-500">
                Profile options will be available after account settings are connected.
              </p>
              <Link
                className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-slate-300 hover:bg-white/5 hover:text-white"
                onClick={() => setAccountMenuOpen(false)}
                role="menuitem"
                to="/settings"
              >
                <Settings className="h-4 w-4" />
                Security & Settings
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}

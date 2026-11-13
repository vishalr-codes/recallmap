import { X } from 'lucide-react';
import { NavLink } from 'react-router-dom';
import { navigationGroups } from '../../app/navigation';

type SidebarProps = {
  mobileOpen: boolean;
  onClose: () => void;
};

export function Sidebar({ mobileOpen, onClose }: SidebarProps) {
  return (
    <>
      {mobileOpen && (
        <button
          aria-label="Close navigation"
          className="fixed inset-0 z-40 bg-black/60 lg:hidden"
          onClick={onClose}
        />
      )}
      <aside
        className={`fixed inset-y-0 left-0 z-50 flex w-[260px] flex-col border-r border-white/[0.07] bg-[#080d0b] transition-transform duration-200 lg:translate-x-0 ${
          mobileOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="flex h-[72px] items-center justify-between border-b border-white/[0.07] px-5">
          <NavLink to="/" onClick={onClose} className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-emerald-400 text-emerald-950">
              <span aria-hidden="true" className="text-sm font-bold tracking-tight">R</span>
            </div>
            <div>
              <div className="text-sm font-semibold tracking-[0.12em] text-white">RECALLMAP</div>
              <div className="text-[11px] text-slate-500">Data lifecycle intelligence</div>
            </div>
          </NavLink>
          <button
            aria-label="Close navigation"
            className="rounded-lg p-2 text-slate-400 hover:bg-white/5 hover:text-white lg:hidden"
            onClick={onClose}
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        <nav aria-label="Primary navigation" className="flex-1 space-y-6 overflow-y-auto px-3 py-5">
          {navigationGroups.map((group) => (
            <div key={group.label}>
              <div className="mb-2 px-3 text-[10px] font-semibold uppercase tracking-[0.18em] text-slate-500">
                {group.label}
              </div>
              <ul className="space-y-1">
                {group.pages.map((page) => {
                  const Icon = page.icon;
                  return (
                    <li key={page.path}>
                      <NavLink
                        to={page.path}
                        end={page.path === '/'}
                        onClick={onClose}
                        className={({ isActive }) =>
                          `flex min-h-10 items-center gap-3 rounded-xl px-3 text-[13px] transition-colors ${
                            isActive
                              ? 'bg-emerald-400/[0.11] font-medium text-emerald-200'
                              : 'text-slate-400 hover:bg-white/[0.045] hover:text-slate-100'
                          }`
                        }
                      >
                        <Icon aria-hidden="true" className="h-4 w-4 shrink-0" strokeWidth={1.8} />
                        <span className="truncate">{page.title}</span>
                      </NavLink>
                    </li>
                  );
                })}
              </ul>
            </div>
          ))}
        </nav>

        <div className="border-t border-white/[0.07] p-4">
          <p className="text-xs leading-5 text-slate-500">Every data trail has an ending. Prove it.</p>
        </div>
      </aside>
    </>
  );
}

import { lazy, Suspense } from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AppShell } from '../components/layout/AppShell';
import { ModulePage } from '../features/shared/ModulePage';
import { appPages } from './navigation';

const CommandCenterPage = lazy(() => import('../features/command-center/CommandCenterPage'));

function PageLoading() {
  return (
    <div aria-busy="true" aria-live="polite" className="mx-auto max-w-5xl py-10">
      <div className="h-7 w-48 animate-pulse rounded-lg bg-white/[0.06]" />
      <div className="mt-5 h-40 animate-pulse rounded-2xl border border-white/[0.08] bg-white/[0.025]" />
      <span className="sr-only">Loading page</span>
    </div>
  );
}

function PageNotFound() {
  return (
    <div className="mx-auto max-w-2xl py-20 text-center">
      <p className="text-sm font-medium text-emerald-300">404 · Route not found</p>
      <h1 className="mt-3 text-2xl font-semibold text-white">This page is not part of this workspace.</h1>
      <p className="mt-2 text-sm text-slate-400">Choose a module from the navigation to continue.</p>
    </div>
  );
}

export function AppRouter() {
  const modulePages = appPages.filter((page) => page.path !== '/');

  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppShell />}>
          <Route
            index
            element={
              <Suspense fallback={<PageLoading />}>
                <CommandCenterPage />
              </Suspense>
            }
          />
          {modulePages.map((page) => (
            <Route
              key={page.path}
              path={page.path.slice(1)}
              element={<ModulePage page={page} />}
            />
          ))}
          <Route path="*" element={<PageNotFound />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

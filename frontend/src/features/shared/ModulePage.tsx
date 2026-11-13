import { ArrowRight, CircleHelp } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { AppPage } from '../../app/navigation';
import { Surface } from '../../components/ui/Surface';

type ModulePageProps = {
  page: AppPage;
};

const endpointStatusLabel: Record<AppPage['endpointStatus'], string> = {
  available: 'Backend endpoint exists · frontend integration pending',
  partial: 'Backend support is partial · remaining workflow pending',
  planned: 'Backend endpoint is not implemented yet',
};

export function ModulePage({ page }: ModulePageProps) {
  const Icon = page.icon;

  return (
    <div className="mx-auto max-w-5xl">
      <div className="mb-7 flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex items-start gap-4">
          <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-emerald-400/15 bg-emerald-400/[0.08] text-emerald-200">
            <Icon aria-hidden="true" className="h-5 w-5" strokeWidth={1.8} />
          </div>
          <div>
            <p className="mb-1 text-[11px] font-semibold uppercase tracking-[0.17em] text-emerald-300/80">
              Workspace module
            </p>
            <h1 className="text-2xl font-semibold tracking-tight text-white sm:text-[28px]">{page.title}</h1>
            <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-400">{page.description}</p>
          </div>
        </div>
        <span className="inline-flex w-fit items-center gap-2 rounded-lg border border-white/[0.08] bg-white/[0.025] px-3 py-2 text-xs text-slate-400">
          <span className="h-1.5 w-1.5 rounded-full bg-amber-300" />
          Implementation in progress
        </span>
      </div>

      <Surface className="p-5 sm:p-7">
        <div className="flex items-start gap-3">
          <CircleHelp className="mt-0.5 h-5 w-5 shrink-0 text-slate-500" />
          <div>
            <h2 className="text-sm font-semibold text-slate-100">Module foundation</h2>
            <p className="mt-2 max-w-3xl text-sm leading-6 text-slate-400">
              This route and its shared workspace layout are in place. This page does not display sample
              records or claim live status. The next implementation step is to connect this module to its
              persisted backend workflow.
            </p>
          </div>
        </div>
        <div className="mt-6 rounded-xl border border-white/[0.07] bg-[#080d0b] p-4">
          <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-slate-500">API readiness</p>
          <p className="mt-2 text-sm text-slate-300">{endpointStatusLabel[page.endpointStatus]}</p>
          {page.endpoint && (
            <code className="mt-3 inline-block break-all rounded-md border border-white/[0.07] bg-white/[0.03] px-2 py-1.5 text-xs text-emerald-200">
              {page.endpoint}
            </code>
          )}
        </div>
        <div className="mt-6 flex flex-wrap gap-3">
          <Link
            className="inline-flex min-h-10 items-center justify-center gap-2 rounded-xl border border-white/10 bg-white/[0.04] px-3.5 text-sm font-medium text-slate-200 transition-colors hover:border-emerald-500/30 hover:bg-white/[0.07]"
            to="/"
          >
            Return to Command Center
            <ArrowRight className="h-4 w-4" />
          </Link>
        </div>
      </Surface>
    </div>
  );
}

import { lazy, Suspense, type FormEvent, useMemo, useState } from 'react';
import { motion, useReducedMotion } from 'framer-motion';
import { Link } from 'react-router-dom';
import { Activity, Database, FileSearch, ShieldCheck, Sparkles, TrendingUp } from 'lucide-react';
import { alerts, dependencyRows, metrics } from '../../data/demo';
import { Button } from '../../components/ui/Button';

type AuthMode = 'login' | 'register';

type AuthResponse = {
  token: string;
  tokenType: string;
  email: string;
  displayName: string;
  organizationId: string;
  workspaceId: string;
};

const LineageGraph = lazy(() => import('./components/LineageGraph'));
const ActivityChart = lazy(() => import('./components/ActivityChart'));
const RepositoryScene = lazy(() => import('./components/RepositoryScene'));

function VisualizationFallback({ className = 'h-56' }: { className?: string }) {
  return <div aria-label="Loading visualization" aria-busy="true" className={`animate-pulse rounded-xl border border-white/[0.08] bg-white/[0.025] ${className}`} />;
}

const accentClasses: Record<string, string> = {
  emerald: 'text-emerald-300',
  cyan: 'text-cyan-300',
  amber: 'text-amber-300',
  rose: 'text-rose-300',
  violet: 'text-violet-300',
};

function CommandCenterPage() {
  const reduceMotion = useReducedMotion() ?? false;
  const [showRepositoryScene, setShowRepositoryScene] = useState(false);
  const [authMode, setAuthMode] = useState<AuthMode>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [displayName, setDisplayName] = useState('Ops lead');
  const [organizationName, setOrganizationName] = useState('Northstar Labs');
  const [token, setToken] = useState('');
  const [workspaceId, setWorkspaceId] = useState('');
  const [workspaceLabel, setWorkspaceLabel] = useState('Demo workspace');
  const [authState, setAuthState] = useState<'idle' | 'loading' | 'error'>('idle');
  const [authMessage, setAuthMessage] = useState('Use this form to call the authentication API. Dashboard records remain synthetic demo data.');

  const sessionBadge = useMemo(
    () => (token ? 'Account authenticated · sample data' : 'Demo workspace'),
    [token],
  );

  async function handleAuthSubmit(event: FormEvent) {
    event.preventDefault();
    setAuthState('loading');
    setAuthMessage('Authenticating against the RecallMap API…');

    try {
      const payload = authMode === 'login'
        ? { email, password }
        : { email, password, displayName, organizationName };

      const response = await fetch(`/api/v1/auth/${authMode === 'login' ? 'login' : 'register'}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        throw new Error(`Authentication failed (${response.status})`);
      }

      const result = (await response.json()) as AuthResponse;
      setToken(result.token);
      setWorkspaceId(result.workspaceId);
      setWorkspaceLabel('Account authenticated');
      setAuthState('idle');
      setAuthMessage(`Authenticated as ${result.displayName}. Dashboard records remain demo data.`);
    } catch (error) {
      setAuthState('error');
      setAuthMessage(error instanceof Error ? error.message : 'Unable to authenticate with the backend right now.');
    }
  }

  return (
    <div className="min-h-screen bg-[#020a07] text-slate-100">
      <div className="mx-auto max-w-7xl px-4 py-2 sm:px-6 lg:px-8">
        <motion.header
          initial={{ opacity: 0, y: -12 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-6 flex flex-col gap-4 rounded-2xl border border-white/[0.08] bg-[#0c1310] px-5 py-4 md:flex-row md:items-center md:justify-between"
        >
          <div>
            <div className="mb-2 flex items-center gap-2 text-xs uppercase tracking-[0.28em] text-emerald-300/80">
              <Sparkles className="h-4 w-4" />
              Emerald Data Observatory
            </div>
            <h1 className="text-2xl font-semibold tracking-tight text-white md:text-3xl">Command Center</h1>
          </div>

          <div className="flex items-center gap-3 self-start md:self-auto">
            <div className="rounded-full border border-emerald-500/30 bg-emerald-500/10 px-3 py-1 text-xs text-emerald-200">
              {sessionBadge}
            </div>
            <Link
              className="inline-flex min-h-10 items-center rounded-xl bg-emerald-400 px-4 text-sm font-semibold text-emerald-950 transition hover:bg-emerald-300"
              to="/deletion/simulator"
            >
              Open simulator
            </Link>
          </div>
        </motion.header>

        <section className="mb-8 grid gap-6 lg:grid-cols-[1.15fr_0.85fr]">
          <div className="rounded-3xl border border-white/10 bg-slate-950/70 p-4 backdrop-blur-xl">
            <div className="mb-4 flex items-center justify-between">
              <div>
                <p className="text-xs uppercase tracking-[0.2em] text-emerald-300">Workspace access</p>
                <h2 className="mt-2 text-xl font-semibold text-white">Authentication API</h2>
              </div>
              <div className="rounded-full border border-emerald-500/30 bg-emerald-500/10 px-2.5 py-1 text-[10px] uppercase tracking-[0.2em] text-emerald-200">
                {token ? 'authorized' : 'demo'}
              </div>
            </div>

            <form onSubmit={handleAuthSubmit} className="space-y-4">
              <div className="flex gap-2 rounded-full border border-white/10 bg-white/5 p-1">
                <button
                  type="button"
                  onClick={() => setAuthMode('login')}
                  className={`flex-1 rounded-full px-3 py-2 text-sm font-medium transition ${authMode === 'login' ? 'bg-emerald-500/15 text-emerald-200' : 'text-slate-300'}`}
                >
                  Login
                </button>
                <button
                  type="button"
                  onClick={() => setAuthMode('register')}
                  className={`flex-1 rounded-full px-3 py-2 text-sm font-medium transition ${authMode === 'register' ? 'bg-emerald-500/15 text-emerald-200' : 'text-slate-300'}`}
                >
                  Register
                </button>
              </div>

              {authMode === 'register' && (
                <div className="grid gap-3 sm:grid-cols-2">
                  <label className="block text-sm text-slate-300">
                    Display name
                    <input value={displayName} onChange={(event) => setDisplayName(event.target.value)} className="mt-1 w-full rounded-xl border border-white/10 bg-slate-900/80 px-3 py-2 text-white outline-none ring-0 placeholder:text-slate-500 focus:border-emerald-500/50" placeholder="Ops lead" />
                  </label>
                  <label className="block text-sm text-slate-300">
                    Organization
                    <input value={organizationName} onChange={(event) => setOrganizationName(event.target.value)} className="mt-1 w-full rounded-xl border border-white/10 bg-slate-900/80 px-3 py-2 text-white outline-none ring-0 placeholder:text-slate-500 focus:border-emerald-500/50" placeholder="Northstar Labs" />
                  </label>
                </div>
              )}

              <label className="block text-sm text-slate-300">
                Email
                <input type="email" autoComplete="email" required value={email} onChange={(event) => setEmail(event.target.value)} className="mt-1 w-full rounded-xl border border-white/10 bg-slate-900/80 px-3 py-2 text-white outline-none ring-0 placeholder:text-slate-500 focus:border-emerald-500/50" placeholder="you@company.com" />
              </label>

              <label className="block text-sm text-slate-300">
                Password
                <input type="password" autoComplete={authMode === 'login' ? 'current-password' : 'new-password'} minLength={8} required value={password} onChange={(event) => setPassword(event.target.value)} className="mt-1 w-full rounded-xl border border-white/10 bg-slate-900/80 px-3 py-2 text-white outline-none ring-0 placeholder:text-slate-500 focus:border-emerald-500/50" placeholder="At least 8 characters" />
              </label>

              <div className="flex items-center justify-between gap-3 pt-2">
                <p className={`text-xs ${authState === 'error' ? 'text-rose-300' : 'text-slate-400'}`}>{authMessage}</p>
                <button type="submit" disabled={authState === 'loading'} className="rounded-full bg-emerald-500 px-4 py-2 text-sm font-semibold text-slate-950 transition hover:bg-emerald-400 disabled:opacity-70">
                  {authState === 'loading' ? 'Connecting…' : authMode === 'login' ? 'Open workspace' : 'Create account'}
                </button>
              </div>
            </form>
          </div>

          <div className="rounded-3xl border border-white/10 bg-slate-950/70 p-4 backdrop-blur-xl">
            <div className="mb-4 flex items-center justify-between">
              <div>
                <p className="text-xs uppercase tracking-[0.2em] text-emerald-300">Current context</p>
                <h3 className="mt-2 text-xl font-semibold text-white">{workspaceLabel}</h3>
              </div>
              <Database className="h-5 w-5 text-emerald-300" />
            </div>

            <ul className="space-y-3 text-sm text-slate-300">
              <li className="rounded-2xl border border-white/10 bg-white/5 px-3 py-2">Workspace ID <span className="float-right text-emerald-200">{workspaceId || 'not selected'}</span></li>
              <li className="rounded-2xl border border-white/10 bg-white/5 px-3 py-2">Authorization <span className="float-right text-emerald-200">{token ? 'JWT active' : 'demo only'}</span></li>
              <li className="rounded-2xl border border-white/10 bg-white/5 px-3 py-2">Dashboard dataset <span className="float-right text-amber-200">synthetic demo</span></li>
            </ul>
          </div>
        </section>

        <section className="mb-8 grid gap-4 md:grid-cols-2 xl:grid-cols-5">
          {metrics.map((metric) => (
            <motion.article
              key={metric.label}
              initial={{ opacity: 0, y: 18 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.35 }}
              className="rounded-2xl border border-white/[0.08] bg-[#0c1310] p-4"
            >
              <div className="mb-5 text-xs uppercase tracking-[0.14em] text-slate-400">{metric.label}</div>
              <div className={`mb-1 text-3xl font-semibold ${accentClasses[metric.accent]}`}>{metric.value}</div>
              <div className="text-sm text-slate-400">{metric.delta}</div>
            </motion.article>
          ))}
        </section>

        <section className="mb-8 grid gap-6 xl:grid-cols-[1.4fr_0.6fr]">
          <motion.div
            initial={{ opacity: 0, y: 24 }}
            animate={{ opacity: 1, y: 0 }}
            className="rounded-2xl border border-white/[0.08] bg-[#0c1310] p-4"
          >
            <div className="mb-4 flex items-center justify-between gap-3">
              <div>
                <p className="text-xs uppercase tracking-[0.16em] text-emerald-300">Visual graph · demo</p>
                <h2 className="mt-2 text-xl font-semibold text-white">Dependency map</h2>
              </div>
              <div className="flex items-center gap-2 rounded-full border border-emerald-500/30 bg-emerald-500/10 px-3 py-1 text-sm text-emerald-200">
                <TrendingUp className="h-4 w-4" />
                Sample graph
              </div>
            </div>

            <Suspense fallback={<VisualizationFallback className="h-[420px]" />}>
              <LineageGraph reduceMotion={reduceMotion} />
            </Suspense>
          </motion.div>

          <motion.aside
            initial={{ opacity: 0, x: 18 }}
            animate={{ opacity: 1, x: 0 }}
            className="space-y-6"
          >
            <div className="rounded-2xl border border-white/[0.08] bg-[#0c1310] p-4">
              <div className="mb-4 flex items-center justify-between">
                <h3 className="text-lg font-semibold text-white">Impact watchlist</h3>
                <ShieldCheck className="h-5 w-5 text-emerald-300" />
              </div>
              <div className="space-y-3">
                {alerts.map((alert) => (
                  <div key={alert.title} className="rounded-2xl border border-white/10 bg-white/5 p-3">
                    <div className="mb-2 flex items-center justify-between gap-3">
                      <span className={`inline-flex rounded-full px-2 py-0.5 text-[10px] font-semibold uppercase tracking-[0.2em] ${alert.severity === 'High' ? 'bg-rose-500/15 text-rose-200' : 'bg-amber-500/15 text-amber-200'}`}>
                        {alert.severity}
                      </span>
                      <span className="rounded-full border border-emerald-500/20 bg-emerald-500/10 px-2 py-0.5 text-[10px] uppercase tracking-[0.2em] text-emerald-200">
                        {alert.badge}
                      </span>
                    </div>
                    <div className="font-medium text-white">{alert.title}</div>
                    <p className="mt-1 text-sm text-slate-300">{alert.detail}</p>
                  </div>
                ))}
              </div>
            </div>

            <div className="rounded-2xl border border-white/[0.08] bg-[#0c1310] p-4">
              <div className="mb-4 flex items-center justify-between">
                <h3 className="text-lg font-semibold text-white">Evidence ledger · demo</h3>
                <FileSearch className="h-5 w-5 text-cyan-300" />
              </div>
              <p className="rounded-xl border border-amber-400/15 bg-amber-400/[0.06] p-3 text-sm leading-6 text-slate-300">
                These are illustrative states only. No deletion has been executed and no external-system evidence is represented here.
              </p>
            </div>
          </motion.aside>
        </section>

        <section className="mb-8 grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
          <motion.div initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} className="rounded-2xl border border-white/[0.08] bg-[#0c1310] p-4">
            <div className="mb-5 flex items-center justify-between">
              <div>
                <p className="text-xs uppercase tracking-[0.2em] text-emerald-300">Sample activity</p>
                <h3 className="mt-2 text-xl font-semibold text-white">Illustrative trend</h3>
              </div>
              <div className="flex items-center gap-2 rounded-full border border-cyan-500/30 bg-cyan-500/10 px-3 py-1 text-sm text-cyan-200">
                <Activity className="h-4 w-4" />
                Sample series
              </div>
            </div>

            <Suspense fallback={<VisualizationFallback />}>
              <ActivityChart />
            </Suspense>
          </motion.div>

          <motion.div initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} className="rounded-2xl border border-white/[0.08] bg-[#0c1310] p-4">
            <div className="mb-5 flex items-center justify-between">
              <div>
                <p className="text-xs uppercase tracking-[0.16em] text-emerald-300">3D observatory · optional</p>
                <h3 className="mt-2 text-xl font-semibold text-white">Data repository cluster</h3>
              </div>
              <Database className="h-5 w-5 text-emerald-300" />
            </div>

            {showRepositoryScene ? (
              <Suspense fallback={<VisualizationFallback />}>
                <RepositoryScene reduceMotion={reduceMotion} />
              </Suspense>
            ) : (
              <div className="flex h-56 items-center justify-center rounded-xl border border-white/[0.08] bg-[#080d0b] p-5 text-center">
                <div>
                  <p className="text-sm text-slate-400">The 3D preview is optional and loads only when requested.</p>
                  <Button className="mt-4" onClick={() => setShowRepositoryScene(true)} variant="secondary">
                    Load 3D preview
                  </Button>
                </div>
              </div>
            )}
          </motion.div>
        </section>

        <motion.section initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} className="rounded-2xl border border-white/[0.08] bg-[#0c1310] p-4">
          <div className="mb-5 flex items-center justify-between">
            <div>
              <p className="text-xs uppercase tracking-[0.2em] text-emerald-300">Dependency review</p>
              <h3 className="mt-2 text-xl font-semibold text-white">Illustrative dependency sample</h3>
            </div>
            <button disabled title="Reports will be available after the report API is implemented" className="rounded-xl border border-white/10 px-3 py-2 text-xs text-slate-500 disabled:cursor-not-allowed">
              Report export unavailable
            </button>
          </div>

          <div className="overflow-hidden rounded-2xl border border-white/10">
            <table className="min-w-full divide-y divide-white/10 text-left text-sm text-slate-200">
              <thead className="bg-white/5 text-xs uppercase tracking-[0.22em] text-slate-400">
                <tr>
                  <th className="px-4 py-3">Source</th>
                  <th className="px-4 py-3">Target</th>
                  <th className="px-4 py-3">Type</th>
                  <th className="px-4 py-3">Impact</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/10">
                {dependencyRows.map((row) => (
                  <tr key={`${row.source}-${row.target}`} className="bg-slate-950/40 hover:bg-white/5">
                    <td className="px-4 py-3 font-medium text-white">{row.source}</td>
                    <td className="px-4 py-3 text-slate-300">{row.target}</td>
                    <td className="px-4 py-3"><span className="rounded-full border border-cyan-500/30 bg-cyan-500/10 px-2 py-1 text-[10px] uppercase tracking-[0.2em] text-cyan-200">{row.type}</span></td>
                    <td className="px-4 py-3"><span className="rounded-full border border-emerald-500/30 bg-emerald-500/10 px-2 py-1 text-[10px] uppercase tracking-[0.2em] text-emerald-200">{row.status}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </motion.section>
      </div>
    </div>
  );
}

export default CommandCenterPage;

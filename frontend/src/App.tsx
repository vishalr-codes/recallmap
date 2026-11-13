import { type FormEvent, useMemo, useState } from 'react';
import { Canvas } from '@react-three/fiber';
import { Float, MeshDistortMaterial, OrbitControls, RoundedBox } from '@react-three/drei';
import { motion } from 'framer-motion';
import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import ReactFlow, { Background, Controls, MiniMap, MarkerType, Position, type Edge, type Node } from 'reactflow';
import { Activity, Database, FileSearch, ShieldCheck, Sparkles, TrendingUp } from 'lucide-react';
import { alerts, activity, dependencyRows, graphEdges, graphNodes, metrics } from './data/demo';
import 'reactflow/dist/style.css';

type AuthMode = 'login' | 'register';

type AuthResponse = {
  token: string;
  tokenType: string;
  email: string;
  displayName: string;
  organizationId: string;
  workspaceId: string;
};

const nodes: Node[] = graphNodes.map((node) => ({
  ...node,
  data: { ...node.data, label: node.data.label },
  sourcePosition: Position.Right,
  targetPosition: Position.Left,
  draggable: false,
  type: 'default',
}));

const edges: Edge[] = graphEdges.map((edge) => ({
  ...edge,
  type: 'smoothstep',
  markerEnd: { type: MarkerType.ArrowClosed, width: 18, height: 18, color: '#10b981' },
  style: { stroke: '#34d399', strokeWidth: 2 },
  labelStyle: { fill: '#d1fae5', fontWeight: 600 },
}));

const accentClasses: Record<string, string> = {
  emerald: 'from-emerald-500/20 to-emerald-400/5 text-emerald-300 ring-emerald-500/30',
  cyan: 'from-cyan-500/20 to-cyan-400/5 text-cyan-300 ring-cyan-500/30',
  amber: 'from-amber-500/20 to-amber-400/5 text-amber-300 ring-amber-500/30',
  rose: 'from-rose-500/20 to-rose-400/5 text-rose-300 ring-rose-500/30',
  violet: 'from-violet-500/20 to-violet-400/5 text-violet-300 ring-violet-500/30',
};

function App() {
  const [authMode, setAuthMode] = useState<AuthMode>('login');
  const [email, setEmail] = useState('team@recallmap.local');
  const [password, setPassword] = useState('Password123');
  const [displayName, setDisplayName] = useState('Ops lead');
  const [organizationName, setOrganizationName] = useState('Northstar Labs');
  const [token, setToken] = useState('');
  const [workspaceId, setWorkspaceId] = useState('');
  const [workspaceLabel, setWorkspaceLabel] = useState('Demo workspace');
  const [authState, setAuthState] = useState<'idle' | 'loading' | 'error'>('idle');
  const [authMessage, setAuthMessage] = useState('Demo mode is active until the backend is connected.');

  const sessionBadge = useMemo(
    () => (token ? 'Connected to live workspace' : 'Demo workspace'),
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
      setWorkspaceLabel('Live workspace');
      setAuthState('idle');
      setAuthMessage(`Authenticated as ${result.displayName}. Workspace is ready for review.`);
    } catch (error) {
      setAuthState('error');
      setAuthMessage(error instanceof Error ? error.message : 'Unable to authenticate with the backend right now.');
    }
  }

  return (
    <div className="min-h-screen bg-[#020a07] text-slate-100">
      <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
        <motion.header
          initial={{ opacity: 0, y: -12 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-8 flex flex-col gap-4 rounded-3xl border border-emerald-500/20 bg-slate-950/70 px-5 py-4 shadow-glow backdrop-blur-xl md:flex-row md:items-center md:justify-between"
        >
          <div>
            <div className="mb-2 flex items-center gap-2 text-xs uppercase tracking-[0.28em] text-emerald-300/80">
              <Sparkles className="h-4 w-4" />
              Emerald Data Observatory
            </div>
            <h1 className="text-2xl font-semibold tracking-tight text-white md:text-3xl">RecallMap mission control</h1>
          </div>

          <div className="flex items-center gap-3 self-start md:self-auto">
            <div className="rounded-full border border-emerald-500/30 bg-emerald-500/10 px-3 py-1 text-xs text-emerald-200">
              {sessionBadge}
            </div>
            <button className="rounded-full border border-white/10 bg-white/5 px-4 py-2 text-sm font-medium text-slate-200 transition hover:border-emerald-500/40 hover:text-emerald-200">
              Launch deletion review
            </button>
          </div>
        </motion.header>

        <section className="mb-8 grid gap-6 lg:grid-cols-[1.15fr_0.85fr]">
          <div className="rounded-3xl border border-white/10 bg-slate-950/70 p-4 backdrop-blur-xl">
            <div className="mb-4 flex items-center justify-between">
              <div>
                <p className="text-xs uppercase tracking-[0.2em] text-emerald-300">Workspace access</p>
                <h2 className="mt-2 text-xl font-semibold text-white">Secure control plane</h2>
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
                <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} className="mt-1 w-full rounded-xl border border-white/10 bg-slate-900/80 px-3 py-2 text-white outline-none ring-0 placeholder:text-slate-500 focus:border-emerald-500/50" placeholder="team@recallmap.local" />
              </label>

              <label className="block text-sm text-slate-300">
                Password
                <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} className="mt-1 w-full rounded-xl border border-white/10 bg-slate-900/80 px-3 py-2 text-white outline-none ring-0 placeholder:text-slate-500 focus:border-emerald-500/50" placeholder="••••••••" />
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
              <li className="rounded-2xl border border-white/10 bg-white/5 px-3 py-2">Workspace ID <span className="float-right text-emerald-200">{workspaceId || 'demo'}</span></li>
              <li className="rounded-2xl border border-white/10 bg-white/5 px-3 py-2">Authorization <span className="float-right text-emerald-200">{token ? 'JWT active' : 'demo only'}</span></li>
              <li className="rounded-2xl border border-white/10 bg-white/5 px-3 py-2">Verification <span className="float-right text-amber-200">2 pending</span></li>
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
              className={`rounded-2xl border border-white/10 bg-gradient-to-br ${accentClasses[metric.accent]} p-[1px]`}
            >
              <div className="h-full rounded-[15px] bg-[#071711]/90 p-4">
                <div className="mb-6 text-xs uppercase tracking-[0.2em] text-slate-400">{metric.label}</div>
                <div className="mb-2 text-3xl font-semibold text-white">{metric.value}</div>
                <div className="text-sm text-slate-300">{metric.delta}</div>
              </div>
            </motion.article>
          ))}
        </section>

        <section className="mb-8 grid gap-6 xl:grid-cols-[1.4fr_0.6fr]">
          <motion.div
            initial={{ opacity: 0, y: 24 }}
            animate={{ opacity: 1, y: 0 }}
            className="rounded-3xl border border-emerald-500/20 bg-slate-950/70 p-4 shadow-glow backdrop-blur-xl"
          >
            <div className="mb-4 flex items-center justify-between gap-3">
              <div>
                <p className="text-xs uppercase tracking-[0.22em] text-emerald-300">Visual graph</p>
                <h2 className="mt-2 text-xl font-semibold text-white">Dependency map</h2>
              </div>
              <div className="flex items-center gap-2 rounded-full border border-emerald-500/30 bg-emerald-500/10 px-3 py-1 text-sm text-emerald-200">
                <TrendingUp className="h-4 w-4" />
                Live lineage
              </div>
            </div>

            <div className="h-[420px] w-full overflow-hidden rounded-2xl border border-white/10 bg-[radial-gradient(circle_at_top,_rgba(16,185,129,0.18),_transparent_40%),linear-gradient(180deg,_rgba(2,6,23,0.92),_rgba(2,6,23,1))]">
              <ReactFlow nodes={nodes} edges={edges} fitView fitViewOptions={{ padding: 0.25 }} proOptions={{ hideAttribution: true }}>
                <MiniMap nodeColor={() => '#10b981'} pannable className="!bg-slate-950/80 !border !border-emerald-500/20" />
                <Controls className="!bg-slate-900/80 !border !border-emerald-500/20 !text-emerald-100" />
                <Background gap={20} size={1} color="#083a2f" />
              </ReactFlow>
            </div>
          </motion.div>

          <motion.aside
            initial={{ opacity: 0, x: 18 }}
            animate={{ opacity: 1, x: 0 }}
            className="space-y-6"
          >
            <div className="rounded-3xl border border-white/10 bg-slate-950/70 p-4 backdrop-blur-xl">
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

            <div className="rounded-3xl border border-white/10 bg-slate-950/70 p-4 backdrop-blur-xl">
              <div className="mb-4 flex items-center justify-between">
                <h3 className="text-lg font-semibold text-white">Evidence ledger</h3>
                <FileSearch className="h-5 w-5 text-cyan-300" />
              </div>
              <ul className="space-y-3 text-sm text-slate-300">
                <li className="flex items-center justify-between rounded-xl bg-white/5 px-3 py-2"><span>Delete token</span><span className="text-emerald-300">Verified</span></li>
                <li className="flex items-center justify-between rounded-xl bg-white/5 px-3 py-2"><span>Cache purge</span><span className="text-amber-300">Pending</span></li>
                <li className="flex items-center justify-between rounded-xl bg-white/5 px-3 py-2"><span>Billing export</span><span className="text-emerald-300">Audited</span></li>
              </ul>
            </div>
          </motion.aside>
        </section>

        <section className="mb-8 grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
          <motion.div initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} className="rounded-3xl border border-white/10 bg-slate-950/70 p-4 backdrop-blur-xl">
            <div className="mb-5 flex items-center justify-between">
              <div>
                <p className="text-xs uppercase tracking-[0.2em] text-emerald-300">Recent activity</p>
                <h3 className="mt-2 text-xl font-semibold text-white">Operational cadence</h3>
              </div>
              <div className="flex items-center gap-2 rounded-full border border-cyan-500/30 bg-cyan-500/10 px-3 py-1 text-sm text-cyan-200">
                <Activity className="h-4 w-4" />
                7 day window
              </div>
            </div>

            <div className="h-56 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={activity}>
                  <defs>
                    <linearGradient id="signalFill" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#34d399" stopOpacity={0.55} />
                      <stop offset="100%" stopColor="#34d399" stopOpacity={0.08} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid stroke="rgba(148,163,184,0.16)" vertical={false} />
                  <XAxis dataKey="name" stroke="#9ca3af" tickLine={false} axisLine={false} />
                  <YAxis stroke="#9ca3af" tickLine={false} axisLine={false} />
                  <Tooltip
                    contentStyle={{ background: '#020a07', border: '1px solid rgba(16,185,129,0.3)', borderRadius: 16, color: '#e2e8f0' }}
                  />
                  <Area type="monotone" dataKey="value" stroke="#34d399" strokeWidth={3} fill="url(#signalFill)" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </motion.div>

          <motion.div initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} className="rounded-3xl border border-white/10 bg-slate-950/70 p-4 backdrop-blur-xl">
            <div className="mb-5 flex items-center justify-between">
              <div>
                <p className="text-xs uppercase tracking-[0.2em] text-emerald-300">3D observatory</p>
                <h3 className="mt-2 text-xl font-semibold text-white">Data repository cluster</h3>
              </div>
              <Database className="h-5 w-5 text-emerald-300" />
            </div>

            <div className="h-56 overflow-hidden rounded-2xl border border-emerald-500/20 bg-[#020a07]">
              <Canvas camera={{ position: [0, 0, 6], fov: 50 }}>
                <ambientLight intensity={0.9} />
                <pointLight position={[5, 5, 5]} intensity={1.5} color="#34d399" />
                <pointLight position={[-5, -2, 2]} intensity={1.2} color="#7dd3fc" />
                <Float speed={3} rotationIntensity={0.9} floatIntensity={1.2}>
                  <RoundedBox args={[1.8, 1.2, 1.2]} radius={0.14} position={[-1.3, 0.4, 0]}>
                    <MeshDistortMaterial color="#0f766e" emissive="#10b981" emissiveIntensity={0.7} distort={0.2} speed={2} roughness={0.2} />
                  </RoundedBox>
                </Float>
                <Float speed={3.5} rotationIntensity={1.1} floatIntensity={1.3}>
                  <RoundedBox args={[1.6, 1, 1]} radius={0.14} position={[0.5, -0.3, 0.4]}>
                    <MeshDistortMaterial color="#14532d" emissive="#34d399" emissiveIntensity={0.8} distort={0.18} speed={2.2} roughness={0.2} />
                  </RoundedBox>
                </Float>
                <Float speed={4} rotationIntensity={1.2} floatIntensity={1.8}>
                  <RoundedBox args={[1.3, 0.9, 0.9]} radius={0.14} position={[1.8, 1, -0.5]}>
                    <MeshDistortMaterial color="#0d9488" emissive="#22d3ee" emissiveIntensity={1} distort={0.25} speed={2.5} roughness={0.2} />
                  </RoundedBox>
                </Float>
                <OrbitControls enableZoom={false} enablePan={false} autoRotate autoRotateSpeed={1.2} />
              </Canvas>
            </div>
          </motion.div>
        </section>

        <motion.section initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} className="rounded-3xl border border-white/10 bg-slate-950/70 p-4 backdrop-blur-xl">
          <div className="mb-5 flex items-center justify-between">
            <div>
              <p className="text-xs uppercase tracking-[0.2em] text-emerald-300">Dependency review</p>
              <h3 className="mt-2 text-xl font-semibold text-white">Records linked to user target</h3>
            </div>
            <button className="rounded-full border border-emerald-500/30 bg-emerald-500/10 px-3 py-1.5 text-sm text-emerald-200 transition hover:bg-emerald-500/20">
              Export impact report
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

export default App;

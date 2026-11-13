import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { activity } from '../../../data/demo';

export default function ActivityChart() {
  return (
    <div className="h-56 w-full" aria-label="Sample activity chart">
      <ResponsiveContainer width="100%" height="100%">
        <AreaChart data={activity}>
          <defs>
            <linearGradient id="signalFill" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#34d399" stopOpacity={0.4} />
              <stop offset="100%" stopColor="#34d399" stopOpacity={0.04} />
            </linearGradient>
          </defs>
          <CartesianGrid stroke="rgba(148,163,184,0.12)" vertical={false} />
          <XAxis dataKey="name" stroke="#829188" tickLine={false} axisLine={false} />
          <YAxis stroke="#829188" tickLine={false} axisLine={false} />
          <Tooltip
            contentStyle={{
              background: '#0c1310',
              border: '1px solid rgba(255,255,255,0.1)',
              borderRadius: 12,
              color: '#e2e8f0',
            }}
          />
          <Area
            type="monotone"
            dataKey="value"
            stroke="#6ee7b7"
            strokeWidth={2}
            fill="url(#signalFill)"
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}

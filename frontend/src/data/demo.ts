export const metrics = [
  { label: 'Total registered data sources', value: '24', delta: '+4 this week', accent: 'emerald' },
  { label: 'Mapped data relationships', value: '182', delta: '+18 this week', accent: 'cyan' },
  { label: 'Active deletion requests', value: '06', delta: '2 need review', accent: 'amber' },
  { label: 'Failed verification tasks', value: '03', delta: '1 critical', accent: 'rose' },
  { label: 'Orphan records detected', value: '11', delta: '4 high severity', accent: 'violet' },
];

export const activity = [
  { name: 'Mon', value: 28 },
  { name: 'Tue', value: 44 },
  { name: 'Wed', value: 38 },
  { name: 'Thu', value: 59 },
  { name: 'Fri', value: 52 },
  { name: 'Sat', value: 71 },
  { name: 'Sun', value: 64 },
];

export const dependencyRows = [
  { source: 'customer_profile', target: 'account_owner', type: 'MAPPED', status: 'Direct' },
  { source: 'orders_cache', target: 'customer_profile', type: 'SYNCED', status: 'Indirect' },
  { source: 'analytics_fact', target: 'orders_cache', type: 'DERIVED', status: 'Optional' },
  { source: 'support_case', target: 'user_id', type: 'LINKED', status: 'Unknown' },
];

export const alerts = [
  { severity: 'High', title: 'Duplicate owner references across billing and CRM', detail: 'Two records present conflicting ownership edges. Requires review before deletion.', badge: 'demo' },
  { severity: 'Medium', title: 'Caching layer remains linked to customer profile', detail: 'Read-only workspace suggests a stale TTL cache can outlive user removal.', badge: 'demo' },
];

export const graphNodes = [
  { id: 'user', position: { x: 180, y: 80 }, data: { label: 'User profile' }, style: { background: '#0f766e', color: '#ecfeff' } },
  { id: 'orders', position: { x: 30, y: 200 }, data: { label: 'Orders DB' }, style: { background: '#0d9488', color: '#ecfeff' } },
  { id: 'billing', position: { x: 310, y: 220 }, data: { label: 'Billing ledger' }, style: { background: '#134e4a', color: '#ecfeff' } },
  { id: 'cache', position: { x: 200, y: 360 }, data: { label: 'Cache cluster' }, style: { background: '#14532d', color: '#ecfeff' } },
  { id: 'analytics', position: { x: 420, y: 120 }, data: { label: 'Analytics lake' }, style: { background: '#1d4ed8', color: '#dbeafe' } },
  { id: 'support', position: { x: 470, y: 330 }, data: { label: 'Support records' }, style: { background: '#7c3aed', color: '#f3e8ff' } },
];

export const graphEdges = [
  { id: 'e1', source: 'user', target: 'orders', label: 'OWNS', animated: true },
  { id: 'e2', source: 'user', target: 'billing', label: 'BILLS', animated: true },
  { id: 'e3', source: 'orders', target: 'cache', label: 'SYNC', animated: true },
  { id: 'e4', source: 'billing', target: 'analytics', label: 'EXPORT', animated: true },
  { id: 'e5', source: 'cache', target: 'support', label: 'PROXY', animated: true },
  { id: 'e6', source: 'billing', target: 'support', label: 'MANUAL REVIEW', animated: true },
];

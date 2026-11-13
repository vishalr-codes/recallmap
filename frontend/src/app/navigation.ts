import {
  Activity,
  Archive,
  Bell,
  Boxes,
  Database,
  FileClock,
  Fingerprint,
  Gauge,
  GitBranch,
  ListChecks,
  Network,
  ScanSearch,
  Settings,
  ShieldCheck,
  Sparkles,
  Users,
  type LucideIcon,
} from 'lucide-react';

export type AppPage = {
  path: string;
  title: string;
  description: string;
  icon: LucideIcon;
  endpoint?: string;
  endpointStatus: 'available' | 'partial' | 'planned';
};

export type NavigationGroup = {
  label: string;
  pages: AppPage[];
};

export const navigationGroups: NavigationGroup[] = [
  {
    label: 'Observe',
    pages: [
      {
        path: '/',
        title: 'Command Center',
        description: 'A workspace overview of data lifecycle operations.',
        icon: Gauge,
        endpointStatus: 'partial',
      },
      {
        path: '/lineage',
        title: 'Data Lineage',
        description: 'Explore registered entities and their dependency paths.',
        icon: GitBranch,
        endpoint: 'GET /api/v1/lineage/graph',
        endpointStatus: 'available',
      },
      {
        path: '/sources',
        title: 'Data Sources',
        description: 'Register and inspect the systems in a workspace.',
        icon: Database,
        endpoint: 'GET, POST /api/v1/data-sources',
        endpointStatus: 'available',
      },
      {
        path: '/discovery',
        title: 'Data Discovery',
        description: 'Discover records and ownership references across sources.',
        icon: ScanSearch,
        endpointStatus: 'planned',
      },
      {
        path: '/orphans',
        title: 'Orphan Intelligence',
        description: 'Review records with missing or ambiguous ownership.',
        icon: Fingerprint,
        endpoint: 'GET /api/v1/orphans',
        endpointStatus: 'available',
      },
      {
        path: '/passport',
        title: 'Digital Data Passport',
        description: 'Trace a subject’s data footprint and evidence history.',
        icon: Archive,
        endpointStatus: 'planned',
      },
    ],
  },
  {
    label: 'Deletion operations',
    pages: [
      {
        path: '/deletion/workflows',
        title: 'Workflow Studio',
        description: 'Create and manage controlled deletion requests.',
        icon: ListChecks,
        endpoint: 'POST /api/v1/deletion-requests',
        endpointStatus: 'partial',
      },
      {
        path: '/deletion/simulator',
        title: 'Impact Simulator',
        description: 'Estimate direct and indirect dependencies before action.',
        icon: Network,
        endpoint: 'GET /api/v1/simulations',
        endpointStatus: 'available',
      },
      {
        path: '/deletion/planner',
        title: 'Smart Planner',
        description: 'Review a proposed, ordered deletion plan and its risks.',
        icon: Sparkles,
        endpointStatus: 'planned',
      },
      {
        path: '/executions',
        title: 'Execution Monitor',
        description: 'Inspect execution attempts, failures, and retry outcomes.',
        icon: Activity,
        endpoint: 'POST /api/v1/executions/{targetId}/run',
        endpointStatus: 'partial',
      },
      {
        path: '/verification',
        title: 'Verification Center',
        description: 'Review evidence and distinguish verified from unverified.',
        icon: ShieldCheck,
        endpoint: 'GET, POST /api/v1/verifications',
        endpointStatus: 'partial',
      },
    ],
  },
  {
    label: 'Workspace',
    pages: [
      {
        path: '/reports',
        title: 'Analytics & Reports',
        description: 'Build operational reports from persisted workspace data.',
        icon: FileClock,
        endpointStatus: 'planned',
      },
      {
        path: '/integrations',
        title: 'Integrations',
        description: 'Review supported connector capabilities and setup.',
        icon: Boxes,
        endpointStatus: 'planned',
      },
      {
        path: '/team',
        title: 'Team & Workspaces',
        description: 'Manage organization membership and workspace access.',
        icon: Users,
        endpoint: 'GET, POST /api/v1/organizations',
        endpointStatus: 'partial',
      },
      {
        path: '/audit',
        title: 'Audit Ledger',
        description: 'Inspect the append-only, hash-chained activity history.',
        icon: FileClock,
        endpoint: 'GET /api/v1/audit',
        endpointStatus: 'available',
      },
      {
        path: '/notifications',
        title: 'Notifications',
        description: 'Review workflow alerts and verification follow-ups.',
        icon: Bell,
        endpointStatus: 'planned',
      },
      {
        path: '/settings',
        title: 'Security & Settings',
        description: 'Configure account, workspace, and security preferences.',
        icon: Settings,
        endpointStatus: 'planned',
      },
    ],
  },
];

export const appPages = navigationGroups.flatMap((group) => group.pages);

export function getPageForPath(path: string): AppPage | undefined {
  return appPages.find((page) => page.path === path);
}

# RECALLMAP Frontend Architecture

## Current repository map

### Frontend

| Area | Current files | Status |
|---|---|---|
| Browser entry | `frontend/src/main.tsx`, `frontend/src/App.tsx` | React root and router entry |
| Routing | `frontend/src/app/AppRouter.tsx`, `frontend/src/app/navigation.ts` | Dedicated routes and centralized page metadata |
| Persistent shell | `frontend/src/components/layout/` | Responsive sidebar, top navigation, breadcrumbs, workspace context |
| Shared UI | `frontend/src/components/ui/` | Initial Button and Surface primitives; local shadcn-style components, no shadcn dependency |
| Command Center | `frontend/src/features/command-center/CommandCenterPage.tsx` | Existing dashboard retained; values are demo data |
| Other pages | `frontend/src/features/shared/ModulePage.tsx` | Honest implementation-state pages, not mock records |
| Demo datasets | `frontend/src/data/demo.ts` | Synthetic sample metrics, alerts, chart and graph inputs |
| Styling | `frontend/src/index.css`, `frontend/tailwind.config.js` | Obsidian/emerald design tokens, accessibility and reduced motion |
| Tooling | `frontend/package.json`, `frontend/vite.config.ts` | Vite, TypeScript, Tailwind, API proxy, React Router |

There was no router, persistent application shell, feature folder structure, or
frontend test suite before this refactor. The prior `App.tsx` dashboard was moved
to the command-center feature rather than discarded. `App.css`, starter assets,
and the existing data module are retained for deliberate cleanup during a later
pass.

### Backend and persistence

The backend is a Spring Boot modular monolith under
`backend/src/main/java/com/recallmap/`. It currently contains `auth`, `users`,
`organizations`, `datasources`, `lineage`, `simulation`, `orphan`, `deletion`,
`execution`, `verification`, `audit`, `common`, and `configuration` packages.
The baseline relational schema is in
`backend/src/main/resources/db/migration/V1__initial_schema.sql`; it includes
users, organizations, memberships, roles, workspaces, sources, entities,
relationships, flows, change events, deletion requests and targets, executions,
verification results, orphan findings, audit events, and notifications.

### Existing REST surface

| Capability | Current endpoint(s) | Frontend status |
|---|---|---|
| Authentication | `POST /api/v1/auth/register`, `/login` | Existing form retained on Command Center; broader auth session lifecycle remains |
| Organizations/workspaces | `GET`, `POST /api/v1/organizations`; `GET`, `POST /api/v1/organizations/{organizationId}/workspaces` | Workspace/team UI not connected |
| Data sources | `GET`, `POST /api/v1/data-sources?workspaceId=...` | UI not connected |
| Lineage | `GET /api/v1/lineage/graph?workspaceId=...` | Existing dashboard graph is still demo data |
| Simulation | `GET /api/v1/simulations?workspaceId=...&targetReference=...` | UI not connected |
| Orphan detection | `GET /api/v1/orphans?workspaceId=...` | UI not connected |
| Deletion requests | `POST /api/v1/deletion-requests?workspaceId=...` | No list or approval endpoint yet |
| Execution | `POST /api/v1/executions/{targetId}/run` | No execution-list endpoint yet |
| Verification | `GET`, `POST /api/v1/verifications?deletionTargetId=...` | UI not connected |
| Audit | `GET`, `POST /api/v1/audit?workspaceId=...` | UI not connected |

An endpoint existing does not imply that the corresponding frontend flow, tenant
authorization, or operational controls are complete. Production-readiness work
must specifically verify tenant enforcement and authorization at every data
boundary.

## Route inventory

| Route | Module | Current endpoint readiness |
|---|---|---|
| `/` | Command Center | Existing dashboard is sample/demo data |
| `/lineage` | Data Lineage Explorer | Graph API exists; UI integration pending |
| `/sources` | Data Source Management | List/create API exists; UI integration pending |
| `/discovery` | Data Discovery | Backend endpoint planned |
| `/deletion/workflows` | Deletion Workflow Studio | Create API exists; listing/approval flow incomplete |
| `/deletion/simulator` | Impact Simulator | Simulation API exists; UI integration pending |
| `/orphans` | Orphan Data Intelligence | Scanner API exists; UI integration pending |
| `/deletion/planner` | Smart Deletion Planner | Backend endpoint planned |
| `/executions` | Execution Monitoring | Run endpoint exists; query and retry APIs incomplete |
| `/verification` | Verification Center | Evidence API exists; UI integration pending |
| `/passport` | Digital Data Passport | Backend endpoint planned |
| `/reports` | Analytics and Reports | Backend/report generation planned |
| `/integrations` | Integration Marketplace | Connector catalog and test APIs planned |
| `/team` | Team and Workspace Management | Organization/workspace APIs exist; membership management incomplete |
| `/audit` | Audit Logs | Read API exists; UI integration pending |
| `/notifications` | Notifications | Backend/API planned |
| `/settings` | Security and Settings | Backend/API planned |

Unwired module routes explicitly disclose their status and endpoint coverage.
They do not render fabricated records, metrics, or connector health.

## File plan

### Created in the architecture/design-system slice

- `frontend/src/app/AppRouter.tsx` — browser route table and not-found route.
- `frontend/src/app/navigation.ts` — one source for routes, labels, icons, and
  API readiness metadata.
- `frontend/src/components/layout/AppShell.tsx` — persistent responsive layout.
- `frontend/src/components/layout/Sidebar.tsx` — grouped primary navigation.
- `frontend/src/components/layout/TopNavigation.tsx` — breadcrumbs, sandbox
  workspace context, account menu, notification route.
- `frontend/src/components/ui/Button.tsx`,
  `frontend/src/components/ui/Surface.tsx` — initial shared design primitives.
- `frontend/src/features/command-center/components/ActivityChart.tsx`,
  `LineageGraph.tsx`, `RepositoryScene.tsx` — independently loaded dashboard
  visualizations.
- `frontend/src/features/shared/ModulePage.tsx` — explicit status page used
  until the module has real API-backed functionality.
- `frontend/src/features/command-center/CommandCenterPage.tsx` — retained
  dashboard moved out of the application entry point.

### Modified

- `frontend/src/App.tsx` — reduced to the router entry point.
- `frontend/index.html` — product title, description, and browser theme color.
- `frontend/src/index.css`, `frontend/tailwind.config.js` — design tokens,
  Obsidian/emerald palette, accessible focus and reduced-motion behavior.
- `frontend/package.json`, `frontend/package-lock.json` — React Router dependency.
- `README.md` — current state and local-development notes.

### Retain

- `frontend/src/main.tsx` — React bootstrap.
- `frontend/src/data/demo.ts` — synthetic datasets; keep visibly labeled and
  never present them as live API data.
- Existing backend modules, controllers, entities, migration, tests, Vite
  configuration, and documentation. No existing backend functionality is
  removed by the UI refactor.

## Delivery sequence

1. **Architecture and design system (in progress):** route inventory, feature
   boundaries, shell, navigation, accessible palette, shared components.
2. **API client and auth session:** typed API client, in-memory token/session
   handling, error/loading states, workspace context.
3. **Read-only operational modules:** data sources, lineage, orphan findings,
   simulation, verification, and audit pages backed by existing APIs.
4. **Workflow gaps:** add backend query/approval/retry capabilities and connect
   deletion studio, execution monitoring, and evidence-centered reports.
5. **Advanced product modules:** discovery, passport, planner, analytics,
   integrations, notifications, and settings; implement APIs before presenting
   these as operational.
6. **Production readiness:** tenant-isolation/authorization tests, API and
   frontend workflow tests, deployment configuration, and end-to-end checks.

The design system is intentionally implemented with local reusable components
in the existing Tailwind stack. shadcn/ui is a component-generation approach;
this repository has not adopted its generated components or Radix dependencies.
Add them only when a specific interaction requires their behavior.

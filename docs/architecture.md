# RECALLMAP Architecture

## Status and scope

This document defines the architecture for a local-development-first
product. Phases 1-2 established the repository documentation, Spring Boot
build baseline, initial PostgreSQL schema, and an explicitly opt-in synthetic
demo dataset. Phase 3 adds the authentication and organization/workspace
foundation: JWT issuance, password hashing, registration/login endpoints,
member creation, and organization/workspace listing/creation. Phase 4 adds
the lineage foundation: data-source registration, graph-based lineage
querying, and dependency traversal tests for direct/indirect edges and cycle
resolution. Phase 5 adds the simulation and orphan-detection foundation:
read-only impact summaries for deletion candidates and automated orphan
classification for uncertain ownership and duplicate owner relationships.
Phase 6 adds the asynchronous deletion foundation: workspace-scoped deletion
requests, source-target selection, async execution using an explicit demo
connector, and state tracking for retry/failure workflows. Phase 7 adds
the ledger and evidence foundation: append-only audit events with hash
chaining and tamper-evident verification records that capture outcome and
evidence metadata. Completion and policy enforcement remain future work,
while the first release remains a modular monolith with no implicit
production integrations.

## System shape

- **Web client:** React, TypeScript, Vite, React Router, Tailwind CSS,
  Framer Motion, React Flow, Three.js via React Three Fiber/Drei, and
  Recharts. The routed shell and shared design primitives are being built
  before page-by-page API integration.
- **API:** Java and Spring Boot, divided into business modules with
  module-owned services and persistence responsibilities.
- **Database:** PostgreSQL with schema changes managed by Flyway.
- **API contract:** versioned REST endpoints under `/api/v1`, documented with
  OpenAPI. Request validation and a consistent error response format are
  cross-cutting requirements.
- **Local development:** Docker Compose for local services, with no paid API
  or hosted service required.

The API is the authority for authorization, tenant scope, deletion workflow
state, audit evidence, and verification status. The browser never connects
directly to PostgreSQL or to registered data systems.

## Backend module boundaries

| Module | Responsibility |
|---|---|
| `auth` | Registration, authentication, token lifecycle |
| `users` | User profiles and organization membership |
| `organizations` | Organization, workspace, roles, and access policy |
| `datasources` | Source registration and connector configuration metadata |
| `lineage` | Data entities, relationships, flows, graph queries, change history |
| `simulation` | Read-only impact analysis and proposed deletion ordering |
| `orphan` | Missing-parent, broken-link, duplicate-owner, and uncertain-owner findings |
| `deletion` | Deletion request lifecycle and target selection |
| `execution` | Authorized asynchronous connector execution, retry, and idempotency |
| `audit` | Append-only audit events and hash-chain integrity |
| `verification` | Evidence assessment and truthful completion status |
| `notifications` | In-product events and user-facing notification state |
| `common` | Shared API errors, validation, pagination, and time abstractions |
| `configuration` | Security, persistence, API documentation, and environment wiring |

Modules should communicate through service-level interfaces and explicit
domain types. Cross-module database access should not become an alternative
integration API. Keep connector secrets out of logs and audit payloads.

## Data and trust boundaries

Organization and workspace identifiers are security boundaries, not merely
filter parameters. Every read and write must derive or validate tenant scope
against the authenticated principal. Roles gate administrative actions and
destructive execution; server-side authorization is required even when the
client hides controls.

The initial connector scope is registered demonstration connectors only.
Connector adapters must declare their supported operations and the evidence
they can produce. A connector that cannot provide adequate evidence cannot
cause a target to be considered verified. Unknown or ambiguous lineage is
surfaced for review rather than silently treated as safe to delete.

## Core workflows

### Lineage and impact simulation

1. Register a data source and its entities/relationships.
2. Query lineage as a bounded graph traversal with cycle detection.
3. Simulate a user or record target without mutating source systems.
4. Classify dependencies (direct, indirect, optional, or unknown), surface
   manual-review items, and show the proposed target sequence and rationale.

### Deletion and verification

1. Create a request and its selected targets; apply an idempotency key.
2. Require an authorized actor before execution.
3. Enqueue per-target work and record status transitions, attempts, and errors.
4. Retry only under an explicit bounded policy; retain failed outcomes.
5. Assess connector evidence per target and append audit events to a
   tamper-evident hash chain.
6. Mark the overall request complete only when all required targets are
   sufficiently verified. Otherwise report partial, failed, or unverified.

Hash chaining detects modification relative to the retained chain; it does
not, by itself, prevent a privileged operator from rewriting the entire store.
External anchoring and key management are future hardening options, not claims
of the initial ledger.

## API surface (current foundation)

The backend currently exposes the following versioned endpoints. Their
existence does not imply complete tenant authorization or production workflow
coverage. In particular, list endpoints, approval/retry operations, pagination,
and consistent filtering remain incomplete in several modules.

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
GET, POST /api/v1/organizations
GET, POST /api/v1/organizations/{organizationId}/workspaces
GET, POST /api/v1/data-sources
GET /api/v1/lineage/graph
GET /api/v1/simulations
GET /api/v1/orphans
POST /api/v1/deletion-requests
POST /api/v1/executions/{targetId}/run
GET, POST /api/v1/verifications
GET, POST /api/v1/audit
```

Concrete payload shapes are defined alongside the current controllers and
DTOs. The frontend route/API readiness map is maintained in
[Frontend architecture](frontend-architecture.md). API errors use Spring
`ProblemDetail` with stable
`errorCode` and `timestamp` extensions; validation failures include sorted
field violations, while unexpected failures return a generic message without
exception details.

## Database baseline

The initial Flyway migration defines users, organizations, system roles and
memberships, workspaces, sources, entities, relationships, flows, lineage
change events, deletion request/target/execution records, verification
results, orphan findings, audit events, and notifications. It uses UUID
identifiers, foreign keys, tenant-scoped composite references where
applicable, uniqueness/check constraints, query indexes, timestamps, and
optimistic-lock version columns for mutable aggregates. Credential material
is not part of a data-source record.

The synthetic dataset is in a separate Flyway location enabled only by the
explicit `demo` profile. The base migration never seeds a demo organization.
No initial seed account or password is created. The schema provides storage
for planned features; it does not mean their application services or API
behavior exist yet.

## Frontend experience

The default experience is a usable 2D dashboard and interactive graph.
Three-dimensional visualization is progressive enhancement for the landing
page and observatory view; it must not be required to complete core tasks.
Respect reduced-motion preferences and provide a 2D fallback for low-powered
devices and mobile layouts. Loading, error, empty, and demo-data states must
be explicit.

## Planned repository structure

```text
backend/
  build.gradle
  settings.gradle
  src/main/java/<package>/
    auth/ users/ organizations/ datasources/
    lineage/ simulation/ orphan/ deletion/
    execution/ audit/ verification/ notifications/
    common/ configuration/
  src/main/resources/
    application.yml
    db/migration/
  src/test/java/<package>/
frontend/
  package.json
  vite.config.ts
  src/
    app/ components/ features/ lib/ styles/
  tests/
docs/
  architecture.md
  database-erd.mmd
  system-architecture.mmd
docker-compose.yml
.env.example
README.md
```

## Phase gates and acceptance criteria

Each phase is reviewed before starting the next. At each gate, report files
created/modified, run relevant checks, fix regressions, update documentation,
and list limitations.

1. **Architecture and repository setup:** agree on modular monolith, module
   boundaries, repository layout, safety invariants, and delivery gates.
2. **Database and backend foundation (complete):** establish build,
   PostgreSQL/Flyway baseline, normalized initial schema, opt-in demo data,
   validation/error conventions, health check, and OpenAPI metadata.
3. **Authentication and organization management:** secure password hashing,
   JWT authentication, membership/roles, workspace management, and tenant
   isolation tests.
4. **Data lineage engine:** source registry and lineage model/API, graph
   traversal and cycle tests, and dependency-change history.
5. **Simulation and orphan detection:** read-only simulation and findings,
   classifications, evidence, and traversal/scanner tests.
6. **Asynchronous deletion engine:** authorized requests, demo adapters,
   background execution, idempotency, retries, failure handling, and tests.
7. **Audit and verification ledger:** chained audit events, evidence
   assessment, verification reports, and tamper-detection tests.
8. **Frontend dashboard and visual explorer:** responsive operational
   workflows, API integration, and frontend workflow tests.
9. **3D experience and animations:** interactive observatory, reduced-motion
   behavior, 2D fallback, and performance checks.
10. **Testing, optimization, and deployment:** end-to-end lifecycle coverage,
    security review, deployment/local documentation, and known limitations.

## Security and operational invariants

- Use environment-provided secrets and safe local examples; do not commit
  credentials.
- Hash passwords with a framework-supported adaptive password encoder.
- Validate inputs, bound graph traversals and list queries, and return
  consistent errors without leaking secrets.
- Restrict CORS to configured origins; apply practical authentication rate
  limits before public deployment.
- Scope every data access to the authorized organization/workspace.
- Separate simulation from execution and require explicit authorization for
  destructive work.
- Preserve per-target execution and verification states; never infer success
  from a missing response.
- Label all synthetic records, metrics, and connector activity as demo data.

## Deferred decisions

The exact package root, chart library, token revocation strategy, queue
implementation, retry limits, and production hosting choice will be selected
when their implementation phase begins. Keep local development self-contained;
deployment documentation must identify free-tier constraints and a compatible
alternative if a provider's terms change.

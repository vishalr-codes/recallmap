# RECALLMAP

RECALLMAP is a developer-focused platform for mapping data dependencies,
simulating deletion impact, managing deletion workflows, and verifying
outcomes with tamper-evident audit evidence.

> **Development status:** Phases 1-7 are implemented at the service
> foundation level: architecture, backend foundation, authentication/
> organization management, lineage graph APIs, deletion-impact simulation,
> orphan detection, async deletion execution, and an audit/verification
> ledger. JWT registration/login, workspace management, source
> registration, graph traversal, impact summaries, orphan findings,
> request execution, audit hash chaining, and evidence recording are all
> included. The frontend dashboard and 3D experience remain future work.

## Architecture

The initial system is a modular monolith: a Java/Spring Boot API and a
React/TypeScript web application backed by PostgreSQL. Module boundaries keep
lineage, simulation, deletion, execution, audit, and verification concerns
separate without introducing premature microservices.

Phase 4 introduces the lineage module foundation: a registered data-source API,
a graph-based data lineage service, and dependency traversal tests that cover
shared owner relationships, indirect propagation, and cycle detection.

See [Architecture](docs/architecture.md) and the
[system context diagram](docs/system-architecture.mmd).

## Planned repository layout

```text
backend/
  src/main/java/.../
    auth/ users/ organizations/ datasources/
    lineage/ simulation/ orphan/ deletion/
    execution/ audit/ verification/ notifications/
    common/ configuration/
  src/main/resources/db/migration/
  src/test/java/...
frontend/
  src/
    app/ components/ features/ lib/ styles/
  tests/
docs/
  architecture.md
  database-erd.mmd
  system-architecture.mmd
```

## Planned phases

1. Architecture and repository setup
2. Database and backend foundation
3. Authentication and organization management
4. Data lineage engine
5. Simulation and orphan detection
6. Asynchronous deletion engine
7. Audit and verification ledger
8. Frontend dashboard and visual explorer
9. 3D experience and animations
10. Testing, optimization, and deployment

Each phase is gated for review before the next phase begins. See the phase
acceptance criteria in [Architecture](docs/architecture.md).

## Safety and evidence

- Demo data and demo connectors must be clearly identified as such.
- Simulation must not mutate source data.
- Destructive execution must be authorized and idempotent.
- RECALLMAP must not report deletion as complete unless every applicable
  integrated target has adequate execution and verification evidence.
- Credentials and signing secrets belong in environment configuration, never
  in source control.

## Local database

Requirements: JDK 21, Gradle 8.14+, and Docker Compose.

```powershell
Copy-Item .env.example .env
# Edit .env and replace the local-only placeholder password.
docker compose up -d db
$env:DB_URL = "jdbc:postgresql://localhost:5432/recallmap"
$env:DB_USERNAME = "recallmap"
$env:DB_PASSWORD = "<the local password from .env>"
Set-Location backend
gradle test
gradle bootRun
```

Flyway applies the base schema on startup. Synthetic seed records are applied
only when the `demo` Spring profile is explicitly enabled, for example by
setting `$env:SPRING_PROFILES_ACTIVE = "demo"` before `gradle bootRun`.
They do not connect to external systems. The API health check is
`http://localhost:8080/actuator/health`; OpenAPI is available at
`http://localhost:8080/api-docs` and Swagger UI at
`http://localhost:8080/swagger-ui`.

The test profile uses an in-memory H2 database and does not apply the
PostgreSQL migrations. Run backend checks from `backend/` with `gradle test`.

## Current limitations

Authentication and authorization, business REST endpoints, frontend,
connectors, asynchronous execution, audit hashing/verification, and
deployment configuration are not implemented. The database schema is a
baseline for the planned modules, not evidence that these workflows exist.

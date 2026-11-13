# RECALLMAP

RECALLMAP is a developer-focused platform for mapping data dependencies,
simulating deletion impact, managing deletion workflows, and verifying
outcomes with tamper-evident audit evidence.

> **Development status:** The Spring Boot backend contains service/API
> foundations for authentication, organizations/workspaces, data sources,
> lineage, simulation, orphan detection, deletion execution, audit, and
> verification. The React frontend now has a routed application shell and
> initial shared design system. The Command Center remains a labeled demo;
> most module pages are not yet connected to backend data. This is not yet a
> production-ready SaaS platform.

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

## Repository structure

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
    app/
    components/
    features/
    data/
docs/
  architecture.md
  frontend-architecture.md
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

The enterprise transformation is proceeding in bounded stages. See
[Frontend architecture and implementation map](docs/frontend-architecture.md)
and the phase acceptance criteria in [Architecture](docs/architecture.md).

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

Frontend pages beyond the demo Command Center are route foundations and do
not yet present API-backed workflows. Some existing backend controllers expose
only create/run operations without corresponding list, approval, or retry
operations. Tenant authorization must be verified and completed before
production use. Connector execution is demonstration-only; generated evidence
does not establish deletion in a real external system. Deployment automation,
frontend workflow tests, and end-to-end coverage remain outstanding.

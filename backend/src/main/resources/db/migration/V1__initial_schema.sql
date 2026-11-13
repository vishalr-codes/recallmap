CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(320) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX users_email_lower_uq ON users (LOWER(email));

CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(160) NOT NULL,
    slug VARCHAR(80) NOT NULL UNIQUE,
    is_demo BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_key VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(255) NOT NULL,
    is_system BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO roles (role_key, name, description) VALUES
    ('OWNER', 'Owner', 'Full organization control, including membership management.'),
    ('ADMIN', 'Administrator', 'Manage workspaces, sources, and deletion workflows.'),
    ('DEVELOPER', 'Developer', 'Inspect lineage and manage approved operational workflows.'),
    ('VIEWER', 'Viewer', 'Read-only access to workspace data and reports.');

CREATE TABLE organization_memberships (
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles (id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (organization_id, user_id)
);

CREATE INDEX organization_memberships_user_idx ON organization_memberships (user_id);

CREATE TABLE workspaces (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(80) NOT NULL,
    is_demo BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE (organization_id, slug),
    UNIQUE (id, organization_id)
);

CREATE INDEX workspaces_organization_idx ON workspaces (organization_id);

CREATE TABLE data_sources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    connector_key VARCHAR(80) NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'REGISTERED'
        CHECK (status IN ('REGISTERED', 'AVAILABLE', 'UNAVAILABLE', 'DISABLED')),
    description VARCHAR(500),
    is_demo BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE (id, workspace_id),
    UNIQUE (workspace_id, name)
);

CREATE INDEX data_sources_workspace_idx ON data_sources (workspace_id);

CREATE TABLE data_entities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL,
    data_source_id UUID NOT NULL,
    external_key VARCHAR(255) NOT NULL,
    name VARCHAR(160) NOT NULL,
    entity_type VARCHAR(40) NOT NULL,
    description VARCHAR(500),
    has_owner_reference BOOLEAN NOT NULL DEFAULT FALSE,
    is_demo BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (data_source_id, workspace_id)
        REFERENCES data_sources (id, workspace_id) ON DELETE CASCADE,
    UNIQUE (id, workspace_id),
    UNIQUE (data_source_id, external_key)
);

CREATE INDEX data_entities_workspace_idx ON data_entities (workspace_id);

CREATE TABLE data_relationships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL,
    source_entity_id UUID NOT NULL,
    target_entity_id UUID NOT NULL,
    relationship_type VARCHAR(40) NOT NULL,
    classification VARCHAR(16) NOT NULL DEFAULT 'UNKNOWN'
        CHECK (classification IN ('DIRECT', 'INDIRECT', 'OPTIONAL', 'UNKNOWN')),
    description VARCHAR(500),
    is_demo BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_entity_id, workspace_id)
        REFERENCES data_entities (id, workspace_id) ON DELETE CASCADE,
    FOREIGN KEY (target_entity_id, workspace_id)
        REFERENCES data_entities (id, workspace_id) ON DELETE CASCADE,
    CHECK (source_entity_id <> target_entity_id)
);

CREATE INDEX data_relationships_source_idx ON data_relationships (workspace_id, source_entity_id);
CREATE INDEX data_relationships_target_idx ON data_relationships (workspace_id, target_entity_id);

CREATE TABLE data_flows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL,
    source_data_source_id UUID NOT NULL,
    target_data_source_id UUID NOT NULL,
    flow_type VARCHAR(40) NOT NULL,
    description VARCHAR(500),
    is_demo BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_data_source_id, workspace_id)
        REFERENCES data_sources (id, workspace_id) ON DELETE CASCADE,
    FOREIGN KEY (target_data_source_id, workspace_id)
        REFERENCES data_sources (id, workspace_id) ON DELETE CASCADE,
    CHECK (source_data_source_id <> target_data_source_id)
);

CREATE INDEX data_flows_workspace_idx ON data_flows (workspace_id);

CREATE TABLE data_change_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    data_source_id UUID,
    entity_type VARCHAR(40) NOT NULL,
    entity_id UUID,
    change_type VARCHAR(32) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    changed_by UUID REFERENCES users (id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (data_source_id, workspace_id)
        REFERENCES data_sources (id, workspace_id) ON DELETE CASCADE
);

CREATE INDEX data_change_events_workspace_time_idx
    ON data_change_events (workspace_id, created_at DESC);

CREATE TABLE deletion_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces (id) ON DELETE RESTRICT,
    requested_by UUID REFERENCES users (id) ON DELETE SET NULL,
    target_type VARCHAR(16) NOT NULL CHECK (target_type IN ('USER', 'RECORD')),
    target_reference VARCHAR(255) NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'REQUESTED'
        CHECK (status IN ('REQUESTED', 'APPROVED', 'RUNNING', 'PARTIAL', 'COMPLETE', 'FAILED', 'CANCELLED')),
    idempotency_key VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE (workspace_id, idempotency_key),
    UNIQUE (id, workspace_id)
);

CREATE INDEX deletion_requests_workspace_time_idx
    ON deletion_requests (workspace_id, created_at DESC);

CREATE TABLE deletion_targets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL,
    deletion_request_id UUID NOT NULL,
    data_source_id UUID NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'RUNNING', 'SUCCEEDED', 'FAILED', 'UNVERIFIED', 'VERIFIED', 'SKIPPED')),
    required BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (deletion_request_id, workspace_id)
        REFERENCES deletion_requests (id, workspace_id) ON DELETE CASCADE,
    FOREIGN KEY (data_source_id, workspace_id)
        REFERENCES data_sources (id, workspace_id) ON DELETE RESTRICT,
    UNIQUE (deletion_request_id, data_source_id),
    UNIQUE (id, workspace_id)
);

CREATE INDEX deletion_targets_request_idx ON deletion_targets (deletion_request_id);

CREATE TABLE deletion_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    deletion_target_id UUID NOT NULL REFERENCES deletion_targets (id) ON DELETE RESTRICT,
    attempt_number INTEGER NOT NULL CHECK (attempt_number > 0),
    status VARCHAR(24) NOT NULL
        CHECK (status IN ('QUEUED', 'RUNNING', 'SUCCEEDED', 'FAILED', 'CANCELLED')),
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    result_summary VARCHAR(500),
    error_code VARCHAR(80),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (completed_at IS NULL OR started_at IS NOT NULL),
    CHECK (completed_at IS NULL OR completed_at >= started_at),
    UNIQUE (deletion_target_id, attempt_number)
);

CREATE INDEX deletion_executions_target_time_idx
    ON deletion_executions (deletion_target_id, created_at DESC);

CREATE TABLE verification_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    deletion_target_id UUID NOT NULL REFERENCES deletion_targets (id) ON DELETE RESTRICT,
    outcome VARCHAR(24) NOT NULL
        CHECK (outcome IN ('VERIFIED', 'NOT_FOUND', 'FAILED', 'INSUFFICIENT_EVIDENCE')),
    evidence_hash CHAR(64),
    evidence_summary VARCHAR(500) NOT NULL,
    verified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (evidence_hash IS NULL OR evidence_hash ~ '^[0-9a-f]{64}$')
);

CREATE INDEX verification_results_target_time_idx
    ON verification_results (deletion_target_id, created_at DESC);

CREATE TABLE orphan_findings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL,
    data_source_id UUID NOT NULL,
    data_entity_id UUID,
    finding_type VARCHAR(40) NOT NULL
        CHECK (finding_type IN ('MISSING_PARENT', 'BROKEN_RELATIONSHIP', 'DUPLICATE_OWNER', 'UNKNOWN_OWNER')),
    severity VARCHAR(12) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    record_reference VARCHAR(255) NOT NULL,
    evidence_summary VARCHAR(500) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'OPEN'
        CHECK (status IN ('OPEN', 'REVIEWED', 'RESOLVED', 'DISMISSED')),
    fingerprint CHAR(64) NOT NULL,
    detected_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMPTZ,
    FOREIGN KEY (data_source_id, workspace_id)
        REFERENCES data_sources (id, workspace_id) ON DELETE CASCADE,
    FOREIGN KEY (data_entity_id, workspace_id)
        REFERENCES data_entities (id, workspace_id) ON DELETE SET NULL,
    UNIQUE (workspace_id, fingerprint),
    CHECK (fingerprint ~ '^[0-9a-f]{64}$')
);

CREATE INDEX orphan_findings_workspace_status_idx
    ON orphan_findings (workspace_id, status, severity);

CREATE TABLE audit_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces (id) ON DELETE RESTRICT,
    sequence_number BIGSERIAL NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    actor_id UUID REFERENCES users (id) ON DELETE SET NULL,
    payload JSONB NOT NULL DEFAULT '{}'::JSONB,
    previous_hash CHAR(64),
    event_hash CHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (workspace_id, sequence_number),
    CHECK (previous_hash IS NULL OR previous_hash ~ '^[0-9a-f]{64}$'),
    CHECK (event_hash ~ '^[0-9a-f]{64}$')
);

CREATE INDEX audit_events_workspace_time_idx
    ON audit_events (workspace_id, created_at DESC);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    notification_type VARCHAR(40) NOT NULL,
    title VARCHAR(160) NOT NULL,
    message VARCHAR(500) NOT NULL,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX notifications_user_unread_idx
    ON notifications (user_id, created_at DESC) WHERE read_at IS NULL;

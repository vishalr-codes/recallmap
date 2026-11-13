INSERT INTO organizations (id, name, slug, is_demo)
VALUES ('00000000-0000-4000-8000-000000000001', 'RECALLMAP Demo', 'recallmap-demo', TRUE);

INSERT INTO workspaces (id, organization_id, name, slug, is_demo)
VALUES (
    '00000000-0000-4000-8000-000000000002',
    '00000000-0000-4000-8000-000000000001',
    'Sample Data Journey',
    'sample-data-journey',
    TRUE
);

INSERT INTO data_sources (id, workspace_id, name, source_type, connector_key, status, description, is_demo)
VALUES
    ('00000000-0000-4000-8000-000000000010', '00000000-0000-4000-8000-000000000002',
     'Application Database', 'DATABASE', 'demo-postgres', 'AVAILABLE',
     'Synthetic source for the demo workspace; not connected to a live database.', TRUE),
    ('00000000-0000-4000-8000-000000000011', '00000000-0000-4000-8000-000000000002',
     'Session Cache', 'CACHE', 'demo-cache', 'AVAILABLE',
     'Synthetic cache source; no external cache is contacted.', TRUE),
    ('00000000-0000-4000-8000-000000000012', '00000000-0000-4000-8000-000000000002',
     'Analytics Warehouse', 'WAREHOUSE', 'demo-warehouse', 'AVAILABLE',
     'Synthetic analytics source; no external warehouse is contacted.', TRUE);

INSERT INTO data_entities (
    id, workspace_id, data_source_id, external_key, name, entity_type, description,
    has_owner_reference, is_demo
)
VALUES
    ('00000000-0000-4000-8000-000000000020', '00000000-0000-4000-8000-000000000002',
     '00000000-0000-4000-8000-000000000010', 'users', 'User Profiles', 'TABLE',
     'Synthetic user profile records.', TRUE, TRUE),
    ('00000000-0000-4000-8000-000000000021', '00000000-0000-4000-8000-000000000002',
     '00000000-0000-4000-8000-000000000011', 'sessions', 'Session Entries', 'KEYSPACE',
     'Synthetic session cache entries.', TRUE, TRUE),
    ('00000000-0000-4000-8000-000000000022', '00000000-0000-4000-8000-000000000002',
     '00000000-0000-4000-8000-000000000012', 'user_events', 'User Events', 'TABLE',
     'Synthetic analytics events.', TRUE, TRUE);

INSERT INTO data_relationships (
    workspace_id, source_entity_id, target_entity_id, relationship_type, classification, description, is_demo
)
VALUES
    ('00000000-0000-4000-8000-000000000002',
     '00000000-0000-4000-8000-000000000020',
     '00000000-0000-4000-8000-000000000021',
     'CACHE_DERIVATION', 'DIRECT', 'Profile changes populate session cache entries.', TRUE),
    ('00000000-0000-4000-8000-000000000002',
     '00000000-0000-4000-8000-000000000020',
     '00000000-0000-4000-8000-000000000022',
     'EVENT_EXPORT', 'INDIRECT', 'Profile activity is represented in analytics events.', TRUE);

INSERT INTO data_flows (
    workspace_id, source_data_source_id, target_data_source_id, flow_type, description, is_demo
)
VALUES
    ('00000000-0000-4000-8000-000000000002',
     '00000000-0000-4000-8000-000000000010',
     '00000000-0000-4000-8000-000000000011',
     'CACHE_WRITE', 'Application database to session cache (synthetic flow).', TRUE),
    ('00000000-0000-4000-8000-000000000002',
     '00000000-0000-4000-8000-000000000010',
     '00000000-0000-4000-8000-000000000012',
     'ANALYTICS_EXPORT', 'Application database to analytics warehouse (synthetic flow).', TRUE);

INSERT INTO data_change_events (
    workspace_id, data_source_id, entity_type, entity_id, change_type, summary
)
VALUES (
    '00000000-0000-4000-8000-000000000002',
    '00000000-0000-4000-8000-000000000010',
    'DATA_SOURCE',
    '00000000-0000-4000-8000-000000000010',
    'REGISTERED',
    'Synthetic application database source registered in the demo workspace.'
);

INSERT INTO deletion_requests (
    id, workspace_id, target_type, target_reference, status, idempotency_key
)
VALUES (
    '00000000-0000-4000-8000-000000000003',
    '00000000-0000-4000-8000-000000000002',
    'USER',
    'demo-user-001',
    'REQUESTED',
    'demo-seed-request-001'
);

INSERT INTO deletion_targets (id, workspace_id, deletion_request_id, data_source_id)
VALUES
    ('00000000-0000-4000-8000-000000000030', '00000000-0000-4000-8000-000000000002',
     '00000000-0000-4000-8000-000000000003', '00000000-0000-4000-8000-000000000010'),
    ('00000000-0000-4000-8000-000000000031', '00000000-0000-4000-8000-000000000002',
     '00000000-0000-4000-8000-000000000003', '00000000-0000-4000-8000-000000000011'),
    ('00000000-0000-4000-8000-000000000032', '00000000-0000-4000-8000-000000000002',
     '00000000-0000-4000-8000-000000000003', '00000000-0000-4000-8000-000000000012');

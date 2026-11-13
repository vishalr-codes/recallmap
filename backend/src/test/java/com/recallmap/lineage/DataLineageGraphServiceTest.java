package com.recallmap.lineage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.recallmap.datasources.DataSourceEntity;
import com.recallmap.organizations.WorkspaceEntity;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataLineageGraphServiceTest {

    private DataEntityRepository dataEntityRepository;
    private DataRelationshipRepository relationshipRepository;
    private DataLineageGraphService service;

    @BeforeEach
    void setUp() {
        dataEntityRepository = mock(DataEntityRepository.class);
        relationshipRepository = mock(DataRelationshipRepository.class);
        service = new DataLineageGraphService(dataEntityRepository, relationshipRepository);
    }

    @Test
    void buildsGraphAndIdentifiesDirectAndIndirectRelationships() {
        UUID workspaceId = UUID.randomUUID();
        DataEntityEntity user = newEntity(workspaceId, "user-1", "USER");
        DataEntityEntity profile = newEntity(workspaceId, "profile-1", "PROFILE");
        DataEntityEntity session = newEntity(workspaceId, "session-1", "SESSION");

        when(dataEntityRepository.findByWorkspace_IdOrderByNameAsc(workspaceId))
                .thenReturn(List.of(user, profile, session));
        when(relationshipRepository.findByWorkspace_IdOrderByCreatedAtDesc(workspaceId))
                .thenReturn(List.of(
                        relationship(user, profile, "OWNS", "DIRECT"),
                        relationship(profile, session, "HAS_SESSION", "INDIRECT"),
                        relationship(user, session, "HAS_SESSION", "DIRECT")));

        DataLineageGraphService.GraphResponse graph = service.buildGraph(workspaceId);

        assertThat(graph.entityCount()).isEqualTo(3);
        assertThat(graph.relationshipCount()).isEqualTo(3);
        assertThat(graph.edges()).extracting(DataLineageGraphService.GraphEdge::classification)
                .containsExactlyInAnyOrder("DIRECT", "INDIRECT", "DIRECT");
        assertThat(service.findReachableEntities(workspaceId, user.getId()))
                .contains(profile.getId(), session.getId());
    }

    @Test
    void detectsCycleNodes() {
        UUID workspaceId = UUID.randomUUID();
        DataEntityEntity alpha = newEntity(workspaceId, "alpha", "ACCOUNT");
        DataEntityEntity beta = newEntity(workspaceId, "beta", "PROFILE");
        when(dataEntityRepository.findByWorkspace_IdOrderByNameAsc(workspaceId))
                .thenReturn(List.of(alpha, beta));
        when(relationshipRepository.findByWorkspace_IdOrderByCreatedAtDesc(workspaceId))
                .thenReturn(List.of(
                        relationship(alpha, beta, "REFERENCES", "DIRECT"),
                        relationship(beta, alpha, "REFERENCES", "DIRECT")));

        assertThat(service.detectCycleNodes(workspaceId)).containsExactlyInAnyOrder(alpha.getId(), beta.getId());
    }

    private DataEntityEntity newEntity(UUID workspaceId, String externalKey, String entityType) {
        DataEntityEntity entity = new DataEntityEntity();
        entity.setId(UUID.randomUUID());
        entity.setWorkspace(newWorkspace(workspaceId));
        entity.setDataSource(newDataSource(entity.getWorkspace()));
        entity.setExternalKey(externalKey);
        entity.setName(externalKey);
        entity.setEntityType(entityType);
        entity.setHasOwnerReference(true);
        return entity;
    }

    private DataRelationshipEntity relationship(DataEntityEntity source, DataEntityEntity target, String relationshipType, String classification) {
        DataRelationshipEntity relationship = new DataRelationshipEntity();
        relationship.setId(UUID.randomUUID());
        relationship.setWorkspace(source.getWorkspace());
        relationship.setSourceEntity(source);
        relationship.setTargetEntity(target);
        relationship.setRelationshipType(relationshipType);
        relationship.setClassification(classification);
        return relationship;
    }

    private WorkspaceEntity newWorkspace(UUID workspaceId) {
        WorkspaceEntity workspace = new WorkspaceEntity();
        workspace.setId(workspaceId);
        workspace.setName("Primary Workspace");
        workspace.setSlug("primary-workspace");
        return workspace;
    }

    private DataSourceEntity newDataSource(WorkspaceEntity workspace) {
        DataSourceEntity dataSource = new DataSourceEntity();
        dataSource.setId(UUID.randomUUID());
        dataSource.setWorkspace(workspace);
        dataSource.setName("Demo-DB");
        dataSource.setSourceType("POSTGRESQL");
        dataSource.setConnectorKey("demo-postgres");
        return dataSource;
    }
}

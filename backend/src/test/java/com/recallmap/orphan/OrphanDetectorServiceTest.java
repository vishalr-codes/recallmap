package com.recallmap.orphan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.recallmap.datasources.DataSourceEntity;
import com.recallmap.lineage.DataEntityEntity;
import com.recallmap.lineage.DataEntityRepository;
import com.recallmap.lineage.DataRelationshipEntity;
import com.recallmap.lineage.DataRelationshipRepository;
import com.recallmap.organizations.WorkspaceEntity;
import com.recallmap.organizations.WorkspaceRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrphanDetectorServiceTest {

    private DataEntityRepository dataEntityRepository;
    private DataRelationshipRepository relationshipRepository;
    private WorkspaceRepository workspaceRepository;
    private OrphanFindingRepository orphanFindingRepository;
    private OrphanDetectorService service;

    @BeforeEach
    void setUp() {
        dataEntityRepository = mock(DataEntityRepository.class);
        relationshipRepository = mock(DataRelationshipRepository.class);
        workspaceRepository = mock(WorkspaceRepository.class);
        orphanFindingRepository = mock(OrphanFindingRepository.class);
        service = new OrphanDetectorService(dataEntityRepository, relationshipRepository, workspaceRepository, orphanFindingRepository);
    }

    @Test
    void detectsUnknownOwnerAndDuplicateOwnerRelationships() {
        UUID workspaceId = UUID.randomUUID();
        WorkspaceEntity workspace = newWorkspace(workspaceId);
        DataEntityEntity noOwner = newEntity(workspace, "invoice-7", false);
        DataEntityEntity primary = newEntity(workspace, "customer-7", true);
        DataEntityEntity duplicate = newEntity(workspace, "profile-7", true);

        when(workspaceRepository.findById(workspaceId)).thenReturn(java.util.Optional.of(workspace));
        when(dataEntityRepository.findByWorkspace_IdOrderByNameAsc(workspaceId)).thenReturn(List.of(noOwner, primary, duplicate));
        when(relationshipRepository.findByWorkspace_IdOrderByCreatedAtDesc(workspaceId)).thenReturn(List.of(
                relation(primary, duplicate, "OWNS", "DIRECT"),
                relation(primary, duplicate, "OWNS", "DIRECT")));
        when(orphanFindingRepository.save(org.mockito.ArgumentMatchers.any(OrphanFindingEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<OrphanFindingSummary> findings = service.detect(workspaceId);

        assertThat(findings).extracting(OrphanFindingSummary::findingType)
                .contains("UNKNOWN_OWNER", "DUPLICATE_OWNER");
    }

    private DataEntityEntity newEntity(WorkspaceEntity workspace, String externalKey, boolean hasOwnerReference) {
        DataEntityEntity entity = new DataEntityEntity();
        entity.setId(UUID.randomUUID());
        entity.setWorkspace(workspace);
        entity.setDataSource(newDataSource(workspace));
        entity.setExternalKey(externalKey);
        entity.setName(externalKey);
        entity.setEntityType("RECORD");
        entity.setHasOwnerReference(hasOwnerReference);
        return entity;
    }

    private DataRelationshipEntity relation(DataEntityEntity source, DataEntityEntity target, String type, String classification) {
        DataRelationshipEntity relation = new DataRelationshipEntity();
        relation.setId(UUID.randomUUID());
        relation.setWorkspace(source.getWorkspace());
        relation.setSourceEntity(source);
        relation.setTargetEntity(target);
        relation.setRelationshipType(type);
        relation.setClassification(classification);
        return relation;
    }

    private WorkspaceEntity newWorkspace(UUID workspaceId) {
        WorkspaceEntity workspace = new WorkspaceEntity();
        workspace.setId(workspaceId);
        workspace.setName("Workspace");
        workspace.setSlug("workspace");
        return workspace;
    }

    private DataSourceEntity newDataSource(WorkspaceEntity workspace) {
        DataSourceEntity source = new DataSourceEntity();
        source.setId(UUID.randomUUID());
        source.setWorkspace(workspace);
        source.setName("Primary Database");
        source.setSourceType("POSTGRESQL");
        source.setConnectorKey("primary-db");
        return source;
    }
}

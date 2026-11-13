package com.recallmap.simulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.recallmap.datasources.DataSourceEntity;
import com.recallmap.lineage.DataEntityEntity;
import com.recallmap.lineage.DataEntityRepository;
import com.recallmap.lineage.DataRelationshipEntity;
import com.recallmap.lineage.DataRelationshipRepository;
import com.recallmap.organizations.WorkspaceEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeletionImpactSimulationServiceTest {

    private DataEntityRepository dataEntityRepository;
    private DataRelationshipRepository dataRelationshipRepository;
    private DeletionImpactSimulationService service;

    @BeforeEach
    void setUp() {
        dataEntityRepository = mock(DataEntityRepository.class);
        dataRelationshipRepository = mock(DataRelationshipRepository.class);
        service = new DeletionImpactSimulationService(dataEntityRepository, dataRelationshipRepository);
    }

    @Test
    void simulatesDirectAndIndirectImpactForTargetEntity() {
        UUID workspaceId = UUID.randomUUID();
        DataEntityEntity root = newEntity(workspaceId, "user-42", "USER");
        DataEntityEntity profile = newEntity(workspaceId, "profile-42", "PROFILE");
        DataEntityEntity setting = newEntity(workspaceId, "setting-42", "SETTING");
        DataEntityEntity audit = newEntity(workspaceId, "audit-42", "AUDIT");

        DataRelationshipEntity rootToProfile = relation(root, profile, "OWNS", "DIRECT");
        DataRelationshipEntity profileToSetting = relation(profile, setting, "HAS_SETTING", "INDIRECT");
        DataRelationshipEntity rootToAudit = relation(root, audit, "HAS_AUDIT", "OPTIONAL");

        when(dataRelationshipRepository.findByWorkspace_IdOrderByCreatedAtDesc(workspaceId))
                .thenReturn(List.of(rootToProfile, profileToSetting, rootToAudit));
        when(dataEntityRepository.findByWorkspace_IdAndId(workspaceId, root.getId())).thenReturn(Optional.of(root));
        when(dataEntityRepository.findByWorkspace_IdAndExternalKey(workspaceId, root.getExternalKey())).thenReturn(Optional.of(root));
        when(dataEntityRepository.findById(root.getId())).thenReturn(Optional.of(root));
        when(dataEntityRepository.findById(profile.getId())).thenReturn(Optional.of(profile));
        when(dataEntityRepository.findById(setting.getId())).thenReturn(Optional.of(setting));
        when(dataEntityRepository.findById(audit.getId())).thenReturn(Optional.of(audit));

        SimulationResult result = service.simulate(workspaceId, new SimulationRequest(root.getExternalKey(), "USER"));

        assertThat(result.directCount()).isEqualTo(1);
        assertThat(result.indirectCount()).isEqualTo(1);
        assertThat(result.optionalCount()).isEqualTo(1);
        assertThat(result.affectedNodes()).hasSize(4);
        assertThat(result.proposedDeletionSequence()).contains("setting-42", "profile-42", "audit-42");
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

    private DataRelationshipEntity relation(DataEntityEntity source, DataEntityEntity target, String type, String classification) {
        DataRelationshipEntity relationship = new DataRelationshipEntity();
        relationship.setId(UUID.randomUUID());
        relationship.setWorkspace(source.getWorkspace());
        relationship.setSourceEntity(source);
        relationship.setTargetEntity(target);
        relationship.setRelationshipType(type);
        relationship.setClassification(classification);
        return relationship;
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

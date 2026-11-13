package com.recallmap.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.recallmap.deletion.DeletionTargetEntity;
import com.recallmap.deletion.DeletionTargetRepository;
import com.recallmap.datasources.DataSourceEntity;
import com.recallmap.organizations.WorkspaceEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VerificationReportServiceTest {

    private VerificationResultRepository repository;
    private DeletionTargetRepository deletionTargetRepository;
    private VerificationReportService service;

    @BeforeEach
    void setUp() {
        repository = mock(VerificationResultRepository.class);
        deletionTargetRepository = mock(DeletionTargetRepository.class);
        service = new VerificationReportService(repository, deletionTargetRepository);
    }

    @Test
    void createsTamperEvidentVerificationEvidence() {
        UUID targetId = UUID.randomUUID();
        DeletionTargetEntity target = new DeletionTargetEntity();
        target.setId(targetId);
        target.setWorkspace(newWorkspace());
        target.setDataSource(newDataSource(target.getWorkspace()));

        when(deletionTargetRepository.findById(targetId)).thenReturn(Optional.of(target));
        when(repository.save(any(VerificationResultEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VerificationResultSummary summary = service.recordVerification(targetId, "VERIFIED", "Verified source deletion was observed with evidence token 1342");

        assertThat(summary.outcome()).isEqualTo("VERIFIED");
        assertThat(summary.evidenceHash()).isNotBlank();
        assertThat(summary.evidenceHash().matches("^[0-9a-f]{64}$")).isTrue();
    }

    private WorkspaceEntity newWorkspace() {
        WorkspaceEntity workspace = new WorkspaceEntity();
        workspace.setId(UUID.randomUUID());
        workspace.setName("Workspace");
        workspace.setSlug("workspace");
        return workspace;
    }

    private DataSourceEntity newDataSource(WorkspaceEntity workspace) {
        DataSourceEntity source = new DataSourceEntity();
        source.setId(UUID.randomUUID());
        source.setWorkspace(workspace);
        source.setName("Orders DB");
        source.setSourceType("POSTGRESQL");
        source.setConnectorKey("orders-db");
        return source;
    }
}

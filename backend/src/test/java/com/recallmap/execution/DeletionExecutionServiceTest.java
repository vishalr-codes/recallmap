package com.recallmap.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.recallmap.datasources.DataSourceEntity;
import com.recallmap.deletion.DeletionRequestEntity;
import com.recallmap.deletion.DeletionRequestRepository;
import com.recallmap.deletion.DeletionTargetEntity;
import com.recallmap.deletion.DeletionTargetRepository;
import com.recallmap.organizations.WorkspaceEntity;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeletionExecutionServiceTest {

    private DeletionTargetRepository targetRepository;
    private DeletionExecutionRepository executionRepository;
    private DeletionRequestRepository requestRepository;
    private DemoDeletionConnector connector;
    private DeletionExecutionService service;

    @BeforeEach
    void setUp() {
        targetRepository = mock(DeletionTargetRepository.class);
        executionRepository = mock(DeletionExecutionRepository.class);
        requestRepository = mock(DeletionRequestRepository.class);
        connector = new DemoDeletionConnector();
        service = new DeletionExecutionService(targetRepository, executionRepository, requestRepository, connector);
    }

    @Test
    void succeedsWhenDemoConnectorCompletesAndMarksTargetComplete() throws Exception {
        UUID targetId = UUID.randomUUID();
        WorkspaceEntity workspace = newWorkspace();
        DeletionRequestEntity request = newDeletionRequest(workspace, "user-42");
        DeletionTargetEntity target = newTarget(workspace, request, newDataSource(workspace));
        target.setId(targetId);

        when(targetRepository.findById(targetId)).thenReturn(java.util.Optional.of(target));
        when(executionRepository.findByDeletionTarget_IdOrderByAttemptNumberDesc(targetId)).thenReturn(List.of());
        when(executionRepository.save(any(DeletionExecutionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(targetRepository.save(any(DeletionTargetEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(requestRepository.save(any(DeletionRequestEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(targetRepository.findByDeletionRequest_IdOrderByCreatedAtAsc(request.getId())).thenReturn(List.of(target));

        TargetExecutionResult result = service.executeAsync(targetId).get();

        assertThat(result.success()).isTrue();
        assertThat(result.status()).isEqualTo("SUCCEEDED");
        assertThat(target.getStatus()).isEqualTo("SUCCEEDED");
    }

    @Test
    void failsWhenDemoConnectorRejectsExecution() throws Exception {
        UUID targetId = UUID.randomUUID();
        WorkspaceEntity workspace = newWorkspace();
        DeletionRequestEntity request = newDeletionRequest(workspace, "DEMO_FAILURE");
        DeletionTargetEntity target = newTarget(workspace, request, newDataSource(workspace));
        target.setId(targetId);

        when(targetRepository.findById(targetId)).thenReturn(java.util.Optional.of(target));
        when(executionRepository.findByDeletionTarget_IdOrderByAttemptNumberDesc(targetId)).thenReturn(List.of());
        when(executionRepository.save(any(DeletionExecutionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(targetRepository.save(any(DeletionTargetEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(requestRepository.save(any(DeletionRequestEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(targetRepository.findByDeletionRequest_IdOrderByCreatedAtAsc(request.getId())).thenReturn(List.of(target));

        TargetExecutionResult result = service.executeAsync(targetId).get();

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("DEMO_CONNECTOR_FAILURE");
    }

    private WorkspaceEntity newWorkspace() {
        WorkspaceEntity workspace = new WorkspaceEntity();
        workspace.setId(UUID.randomUUID());
        workspace.setName("Primary Workspace");
        workspace.setSlug("primary-workspace");
        return workspace;
    }

    private DeletionRequestEntity newDeletionRequest(WorkspaceEntity workspace, String targetReference) {
        DeletionRequestEntity request = new DeletionRequestEntity();
        request.setId(UUID.randomUUID());
        request.setWorkspace(workspace);
        request.setTargetType("USER");
        request.setTargetReference(targetReference);
        request.setStatus("REQUESTED");
        request.setIdempotencyKey("key-" + UUID.randomUUID());
        return request;
    }

    private DeletionTargetEntity newTarget(WorkspaceEntity workspace, DeletionRequestEntity request, DataSourceEntity source) {
        DeletionTargetEntity target = new DeletionTargetEntity();
        target.setId(UUID.randomUUID());
        target.setWorkspace(workspace);
        target.setDeletionRequest(request);
        target.setDataSource(source);
        target.setStatus("PENDING");
        target.setRequired(true);
        return target;
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

package com.recallmap.deletion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.recallmap.datasources.DataSourceEntity;
import com.recallmap.datasources.DataSourceRepository;
import com.recallmap.organizations.WorkspaceEntity;
import com.recallmap.organizations.WorkspaceRepository;
import com.recallmap.users.UserEntity;
import com.recallmap.users.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeletionRequestServiceTest {

    private DeletionRequestRepository requestRepository;
    private DeletionTargetRepository targetRepository;
    private WorkspaceRepository workspaceRepository;
    private DataSourceRepository dataSourceRepository;
    private UserRepository userRepository;
    private DeletionRequestService service;

    @BeforeEach
    void setUp() {
        requestRepository = mock(DeletionRequestRepository.class);
        targetRepository = mock(DeletionTargetRepository.class);
        workspaceRepository = mock(WorkspaceRepository.class);
        dataSourceRepository = mock(DataSourceRepository.class);
        userRepository = mock(UserRepository.class);
        service = new DeletionRequestService(requestRepository, targetRepository, workspaceRepository, dataSourceRepository, userRepository);
    }

    @Test
    void createsDeletionRequestAndTracksIdempotency() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        WorkspaceEntity workspace = newWorkspace(workspaceId);
        UserEntity user = new UserEntity();
        user.setId(userId);
        DataSourceEntity source = newDataSource(workspace);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(dataSourceRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(dataSourceRepository.findByWorkspace_IdOrderByNameAsc(workspaceId)).thenReturn(List.of(source));
        when(requestRepository.findByWorkspace_IdAndIdempotencyKey(workspaceId, "req-001")).thenReturn(Optional.empty());
        when(requestRepository.save(any(DeletionRequestEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(targetRepository.save(any(DeletionTargetEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeletionRequestSummary summary = service.createRequest(workspaceId, userId,
                new CreateDeletionRequestRequest("USER", "user-42", "req-001", List.of(source.getId())));

        assertThat(summary.targetReference()).isEqualTo("user-42");
        assertThat(summary.status()).isEqualTo("REQUESTED");
    }

    private WorkspaceEntity newWorkspace(UUID workspaceId) {
        WorkspaceEntity workspace = new WorkspaceEntity();
        workspace.setId(workspaceId);
        workspace.setName("Primary Workspace");
        workspace.setSlug("primary-workspace");
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

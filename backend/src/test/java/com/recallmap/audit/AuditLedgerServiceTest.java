package com.recallmap.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.recallmap.organizations.WorkspaceEntity;
import com.recallmap.organizations.WorkspaceRepository;
import com.recallmap.users.UserEntity;
import com.recallmap.users.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuditLedgerServiceTest {

    private AuditEventRepository repository;
    private WorkspaceRepository workspaceRepository;
    private UserRepository userRepository;
    private AuditLedgerService service;

    @BeforeEach
    void setUp() {
        repository = mock(AuditEventRepository.class);
        workspaceRepository = mock(WorkspaceRepository.class);
        userRepository = mock(UserRepository.class);
        service = new AuditLedgerService(repository, workspaceRepository, userRepository);
    }

    @Test
    void appendsHashChainedAuditEventsForWorkspace() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        WorkspaceEntity workspace = newWorkspace(workspaceId);
        UserEntity actor = new UserEntity();
        actor.setId(userId);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(userRepository.findById(userId)).thenReturn(Optional.of(actor));
        when(repository.findTopByWorkspace_IdOrderBySequenceNumberDesc(workspaceId)).thenReturn(null);
        when(repository.save(org.mockito.ArgumentMatchers.any(AuditEventEntity.class)))
                .thenAnswer(invocation -> {
                    AuditEventEntity event = invocation.getArgument(0);
                    event.setSequenceNumber(1L);
                    return event;
                });

        AuditEventSummary first = service.appendEvent(workspaceId, userId, "DELETE_REQUEST_CREATED", "{\"target\":\"user-42\"}");
        assertThat(first.sequenceNumber()).isEqualTo(1L);
        assertThat(first.previousHash()).isNull();
        assertThat(first.eventHash()).isNotBlank();
        assertThat(first.eventHash().matches("^[0-9a-f]{64}$")).isTrue();

        when(repository.findTopByWorkspace_IdOrderBySequenceNumberDesc(workspaceId)).thenReturn(mockAuditEvent(first));
        when(repository.save(org.mockito.ArgumentMatchers.any(AuditEventEntity.class)))
                .thenAnswer(invocation -> {
                    AuditEventEntity event = invocation.getArgument(0);
                    event.setSequenceNumber(2L);
                    return event;
                });

        AuditEventSummary second = service.appendEvent(workspaceId, userId, "DELETE_REQUEST_COMPLETED", "{\"status\":\"COMPLETE\"}");
        assertThat(second.sequenceNumber()).isEqualTo(2L);
        assertThat(second.previousHash()).isEqualTo(first.eventHash());
    }

    private WorkspaceEntity newWorkspace(UUID workspaceId) {
        WorkspaceEntity workspace = new WorkspaceEntity();
        workspace.setId(workspaceId);
        workspace.setName("Workspace");
        workspace.setSlug("workspace");
        return workspace;
    }

    private AuditEventEntity mockAuditEvent(AuditEventSummary summary) {
        AuditEventEntity event = new AuditEventEntity();
        event.setId(summary.id());
        event.setSequenceNumber(summary.sequenceNumber());
        event.setEventType("DELETE_REQUEST_CREATED");
        event.setPreviousHash(summary.previousHash());
        event.setEventHash(summary.eventHash());
        return event;
    }
}

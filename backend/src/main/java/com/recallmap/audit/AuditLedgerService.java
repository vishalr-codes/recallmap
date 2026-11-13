package com.recallmap.audit;

import com.recallmap.organizations.WorkspaceEntity;
import com.recallmap.organizations.WorkspaceRepository;
import com.recallmap.users.UserEntity;
import com.recallmap.users.UserRepository;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuditLedgerService {

    private final AuditEventRepository auditEventRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    public AuditLedgerService(
            AuditEventRepository auditEventRepository,
            WorkspaceRepository workspaceRepository,
            UserRepository userRepository) {
        this.auditEventRepository = auditEventRepository;
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AuditEventSummary appendEvent(UUID workspaceId, UUID actorId, String eventType, String payload) {
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));
        UserEntity actor = actorId == null ? null : userRepository.findById(actorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String normalizedPayload = payload == null || payload.isBlank() ? "{}" : payload;
        AuditEventEntity previousEvent = auditEventRepository.findTopByWorkspace_IdOrderBySequenceNumberDesc(workspaceId);
        String previousHash = previousEvent == null ? null : previousEvent.getEventHash();
        String eventHash = hashEvent(workspaceId, eventType, normalizedPayload, previousHash, actorId);

        AuditEventEntity event = new AuditEventEntity();
        event.setWorkspace(workspace);
        event.setActor(actor);
        event.setEventType(eventType);
        event.setPayload(normalizedPayload);
        event.setPreviousHash(previousHash);
        event.setEventHash(eventHash);
        event.setSequenceNumber(previousEvent == null ? 1L : previousEvent.getSequenceNumber() + 1L);
        AuditEventEntity saved = auditEventRepository.save(event);
        return AuditEventSummary.fromEntity(saved);
    }

    @Transactional
    public List<AuditEventSummary> listEvents(UUID workspaceId) {
        return auditEventRepository.findByWorkspace_IdOrderBySequenceNumberAsc(workspaceId).stream()
                .map(AuditEventSummary::fromEntity)
                .toList();
    }

    private String hashEvent(UUID workspaceId, String eventType, String payload, String previousHash, UUID actorId) {
        String raw = workspaceId + "|" + eventType + "|" + payload + "|" + (previousHash == null ? "" : previousHash) + "|" + (actorId == null ? "" : actorId);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte value : bytes) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}

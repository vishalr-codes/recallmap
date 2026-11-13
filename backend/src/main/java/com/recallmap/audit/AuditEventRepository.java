package com.recallmap.audit;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {

    List<AuditEventEntity> findByWorkspace_IdOrderBySequenceNumberAsc(UUID workspaceId);

    AuditEventEntity findTopByWorkspace_IdOrderBySequenceNumberDesc(UUID workspaceId);
}

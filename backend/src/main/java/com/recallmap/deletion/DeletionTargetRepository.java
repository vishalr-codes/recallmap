package com.recallmap.deletion;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeletionTargetRepository extends JpaRepository<DeletionTargetEntity, UUID> {

    List<DeletionTargetEntity> findByDeletionRequest_IdOrderByCreatedAtAsc(UUID deletionRequestId);

    List<DeletionTargetEntity> findByWorkspace_IdOrderByCreatedAtDesc(UUID workspaceId);
}

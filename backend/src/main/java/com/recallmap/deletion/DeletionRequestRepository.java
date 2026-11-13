package com.recallmap.deletion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeletionRequestRepository extends JpaRepository<DeletionRequestEntity, UUID> {

    List<DeletionRequestEntity> findByWorkspace_IdOrderByCreatedAtDesc(UUID workspaceId);

    Optional<DeletionRequestEntity> findByWorkspace_IdAndIdempotencyKey(UUID workspaceId, String idempotencyKey);
}

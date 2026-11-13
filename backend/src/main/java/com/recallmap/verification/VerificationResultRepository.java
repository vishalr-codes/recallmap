package com.recallmap.verification;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationResultRepository extends JpaRepository<VerificationResultEntity, UUID> {

    List<VerificationResultEntity> findByDeletionTarget_IdOrderByCreatedAtDesc(UUID deletionTargetId);
}

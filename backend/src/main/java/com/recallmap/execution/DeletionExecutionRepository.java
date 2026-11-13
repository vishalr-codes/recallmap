package com.recallmap.execution;

import com.recallmap.deletion.DeletionTargetEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeletionExecutionRepository extends JpaRepository<DeletionExecutionEntity, UUID> {

    List<DeletionExecutionEntity> findByDeletionTarget_IdOrderByAttemptNumberDesc(UUID deletionTargetId);

    Optional<DeletionExecutionEntity> findByDeletionTargetAndAttemptNumber(DeletionTargetEntity deletionTarget, int attemptNumber);
}

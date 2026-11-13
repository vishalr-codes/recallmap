package com.recallmap.orphan;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrphanFindingRepository extends JpaRepository<OrphanFindingEntity, UUID> {

    List<OrphanFindingEntity> findByWorkspace_IdOrderByDetectedAtDesc(UUID workspaceId);
}

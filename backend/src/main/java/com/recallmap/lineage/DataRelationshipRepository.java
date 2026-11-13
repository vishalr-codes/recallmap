package com.recallmap.lineage;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataRelationshipRepository extends JpaRepository<DataRelationshipEntity, UUID> {

    List<DataRelationshipEntity> findByWorkspace_IdOrderByCreatedAtDesc(UUID workspaceId);

    List<DataRelationshipEntity> findBySourceEntity_Id(UUID sourceEntityId);
}

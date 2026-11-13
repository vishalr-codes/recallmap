package com.recallmap.lineage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataEntityRepository extends JpaRepository<DataEntityEntity, UUID> {

    List<DataEntityEntity> findByWorkspace_IdOrderByNameAsc(UUID workspaceId);

    Optional<DataEntityEntity> findByWorkspace_IdAndExternalKey(UUID workspaceId, String externalKey);

    Optional<DataEntityEntity> findByWorkspace_IdAndId(UUID workspaceId, UUID id);
}

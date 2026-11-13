package com.recallmap.datasources;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataSourceRepository extends JpaRepository<DataSourceEntity, UUID> {

    List<DataSourceEntity> findByWorkspace_IdOrderByNameAsc(UUID workspaceId);

    boolean existsByWorkspace_IdAndNameIgnoreCase(UUID workspaceId, String name);
}

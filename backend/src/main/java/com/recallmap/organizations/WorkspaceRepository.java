package com.recallmap.organizations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<WorkspaceEntity, UUID> {

    List<WorkspaceEntity> findByOrganizationId(UUID organizationId);

    Optional<WorkspaceEntity> findByOrganizationIdAndSlug(UUID organizationId, String slug);
}

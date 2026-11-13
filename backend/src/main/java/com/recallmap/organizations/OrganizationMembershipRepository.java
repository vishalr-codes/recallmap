package com.recallmap.organizations;

import com.recallmap.users.UserEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationMembershipRepository extends JpaRepository<OrganizationMembershipEntity, OrganizationMembershipEntity.OrganizationMembershipId> {

    List<OrganizationMembershipEntity> findByUser(UserEntity user);

    List<OrganizationMembershipEntity> findByOrganizationId(UUID organizationId);
}

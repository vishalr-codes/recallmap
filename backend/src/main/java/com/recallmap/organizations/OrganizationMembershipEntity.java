package com.recallmap.organizations;

import com.recallmap.users.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "organization_memberships")
public class OrganizationMembershipEntity {

    @EmbeddedId
    private OrganizationMembershipId id = new OrganizationMembershipId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("organizationId")
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public OrganizationMembershipId getId() {
        return id;
    }

    public void setId(OrganizationMembershipId id) {
        this.id = id;
    }

    public OrganizationEntity getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationEntity organization) {
        this.organization = organization;
        if (this.id == null) {
            this.id = new OrganizationMembershipId();
        }
        this.id.organizationId = organization.getId();
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
        if (this.id == null) {
            this.id = new OrganizationMembershipId();
        }
        this.id.userId = user.getId();
    }

    public RoleEntity getRole() {
        return role;
    }

    public void setRole(RoleEntity role) {
        this.role = role;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public static class OrganizationMembershipId implements Serializable {
        private UUID organizationId;
        private UUID userId;

        public OrganizationMembershipId() {
        }

        public OrganizationMembershipId(UUID organizationId, UUID userId) {
            this.organizationId = organizationId;
            this.userId = userId;
        }

        public UUID getOrganizationId() {
            return organizationId;
        }

        public void setOrganizationId(UUID organizationId) {
            this.organizationId = organizationId;
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof OrganizationMembershipId that)) {
                return false;
            }
            return Objects.equals(organizationId, that.organizationId)
                    && Objects.equals(userId, that.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(organizationId, userId);
        }
    }
}

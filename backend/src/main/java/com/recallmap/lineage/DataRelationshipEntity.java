package com.recallmap.lineage;

import com.recallmap.organizations.WorkspaceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "data_relationships")
public class DataRelationshipEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkspaceEntity workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_entity_id", nullable = false)
    private DataEntityEntity sourceEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_entity_id", nullable = false)
    private DataEntityEntity targetEntity;

    @Column(name = "relationship_type", nullable = false, length = 40)
    private String relationshipType;

    @Column(name = "classification", nullable = false, length = 16)
    private String classification = "UNKNOWN";

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_demo", nullable = false)
    private boolean demo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public WorkspaceEntity getWorkspace() {
        return workspace;
    }

    public void setWorkspace(WorkspaceEntity workspace) {
        this.workspace = workspace;
    }

    public DataEntityEntity getSourceEntity() {
        return sourceEntity;
    }

    public void setSourceEntity(DataEntityEntity sourceEntity) {
        this.sourceEntity = sourceEntity;
    }

    public DataEntityEntity getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(DataEntityEntity targetEntity) {
        this.targetEntity = targetEntity;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDemo() {
        return demo;
    }

    public void setDemo(boolean demo) {
        this.demo = demo;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}

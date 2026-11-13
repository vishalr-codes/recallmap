package com.recallmap.orphan;

import com.recallmap.datasources.DataSourceEntity;
import com.recallmap.lineage.DataEntityEntity;
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
@Table(name = "orphan_findings")
public class OrphanFindingEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkspaceEntity workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_source_id")
    private DataSourceEntity dataSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_entity_id")
    private DataEntityEntity dataEntity;

    @Column(name = "finding_type", nullable = false, length = 40)
    private String findingType;

    @Column(name = "severity", nullable = false, length = 12)
    private String severity;

    @Column(name = "record_reference", nullable = false, length = 255)
    private String recordReference;

    @Column(name = "evidence_summary", nullable = false, length = 500)
    private String evidenceSummary;

    @Column(name = "status", nullable = false, length = 16)
    private String status = "OPEN";

    @Column(name = "fingerprint", nullable = false, length = 64)
    private String fingerprint;

    @CreationTimestamp
    @Column(name = "detected_at", nullable = false, updatable = false)
    private OffsetDateTime detectedAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

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

    public DataSourceEntity getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSourceEntity dataSource) {
        this.dataSource = dataSource;
    }

    public DataEntityEntity getDataEntity() {
        return dataEntity;
    }

    public void setDataEntity(DataEntityEntity dataEntity) {
        this.dataEntity = dataEntity;
    }

    public String getFindingType() {
        return findingType;
    }

    public void setFindingType(String findingType) {
        this.findingType = findingType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getRecordReference() {
        return recordReference;
    }

    public void setRecordReference(String recordReference) {
        this.recordReference = recordReference;
    }

    public String getEvidenceSummary() {
        return evidenceSummary;
    }

    public void setEvidenceSummary(String evidenceSummary) {
        this.evidenceSummary = evidenceSummary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public OffsetDateTime getDetectedAt() {
        return detectedAt;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(OffsetDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}

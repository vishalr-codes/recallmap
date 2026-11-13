package com.recallmap.orphan;

import com.recallmap.datasources.DataSourceEntity;
import com.recallmap.lineage.DataEntityEntity;
import com.recallmap.lineage.DataEntityRepository;
import com.recallmap.lineage.DataRelationshipEntity;
import com.recallmap.lineage.DataRelationshipRepository;
import com.recallmap.organizations.WorkspaceEntity;
import com.recallmap.organizations.WorkspaceRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OrphanDetectorService {

    private final DataEntityRepository dataEntityRepository;
    private final DataRelationshipRepository relationshipRepository;
    private final WorkspaceRepository workspaceRepository;
    private final OrphanFindingRepository orphanFindingRepository;

    public OrphanDetectorService(
            DataEntityRepository dataEntityRepository,
            DataRelationshipRepository relationshipRepository,
            WorkspaceRepository workspaceRepository,
            OrphanFindingRepository orphanFindingRepository) {
        this.dataEntityRepository = dataEntityRepository;
        this.relationshipRepository = relationshipRepository;
        this.workspaceRepository = workspaceRepository;
        this.orphanFindingRepository = orphanFindingRepository;
    }

    public List<OrphanFindingSummary> detect(UUID workspaceId) {
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));

        List<DataEntityEntity> entities = dataEntityRepository.findByWorkspace_IdOrderByNameAsc(workspaceId);
        Map<UUID, List<DataRelationshipEntity>> incoming = new HashMap<>();
        for (DataRelationshipEntity relationship : relationshipRepository.findByWorkspace_IdOrderByCreatedAtDesc(workspaceId)) {
            incoming.computeIfAbsent(relationship.getTargetEntity().getId(), key -> new ArrayList<>()).add(relationship);
        }

        List<OrphanFindingEntity> findings = new ArrayList<>();
        for (DataEntityEntity entity : entities) {
            if (!entity.isHasOwnerReference()) {
                findings.add(buildFinding(workspace, entity, "UNKNOWN_OWNER", "MEDIUM",
                        "Entity has no owner reference: " + entity.getName(),
                        "The entity was registered without an owner reference and cannot be confidently associated with a user."));
            }

            List<DataRelationshipEntity> inbound = incoming.getOrDefault(entity.getId(), List.of());
            long duplicateOwnerCount = inbound.stream()
                    .filter(rel -> "OWNS".equalsIgnoreCase(rel.getRelationshipType()) || "BELONGS_TO".equalsIgnoreCase(rel.getRelationshipType()))
                    .count();
            if (duplicateOwnerCount > 1) {
                findings.add(buildFinding(workspace, entity, "DUPLICATE_OWNER", "HIGH",
                        "Multiple owner relationships: " + entity.getName(),
                        "The entity has multiple ownership edges suggesting a conflict or stale duplication."));
            }
        }

        for (DataRelationshipEntity relationship : relationshipRepository.findByWorkspace_IdOrderByCreatedAtDesc(workspaceId)) {
            boolean broken = relationship.getSourceEntity() == null || relationship.getTargetEntity() == null;
            if (broken) {
                findings.add(buildFinding(workspace, null, "BROKEN_RELATIONSHIP", "HIGH",
                        "Broken relationship in workspace " + workspace.getId(),
                        "A relationship record exists without a resolvable source or target entity."));
            }
        }

        List<OrphanFindingEntity> saved = findings.stream()
                .map(orphanFindingRepository::save)
                .toList();

        return saved.stream().map(OrphanFindingSummary::fromEntity).toList();
    }

    private OrphanFindingEntity buildFinding(
            WorkspaceEntity workspace,
            DataEntityEntity entity,
            String findingType,
            String severity,
            String recordReference,
            String evidenceSummary) {
        OrphanFindingEntity finding = new OrphanFindingEntity();
        finding.setWorkspace(workspace);
        finding.setDataEntity(entity);
        finding.setDataSource(entity == null ? null : entity.getDataSource());
        finding.setFindingType(findingType);
        finding.setSeverity(severity);
        finding.setRecordReference(recordReference);
        finding.setEvidenceSummary(evidenceSummary);
        finding.setFingerprint(createFingerprint(findingType, recordReference, evidenceSummary));
        return finding;
    }

    private String createFingerprint(String findingType, String recordReference, String evidenceSummary) {
        String raw = findingType + "|" + recordReference + "|" + evidenceSummary;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte value : hash) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            return Integer.toHexString(raw.hashCode());
        }
    }
}

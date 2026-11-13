package com.recallmap.simulation;

import com.recallmap.datasources.DataSourceEntity;
import com.recallmap.lineage.DataEntityEntity;
import com.recallmap.lineage.DataEntityRepository;
import com.recallmap.lineage.DataRelationshipEntity;
import com.recallmap.lineage.DataRelationshipRepository;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DeletionImpactSimulationService {

    private final DataEntityRepository dataEntityRepository;
    private final DataRelationshipRepository dataRelationshipRepository;

    public DeletionImpactSimulationService(
            DataEntityRepository dataEntityRepository,
            DataRelationshipRepository dataRelationshipRepository) {
        this.dataEntityRepository = dataEntityRepository;
        this.dataRelationshipRepository = dataRelationshipRepository;
    }

    public SimulationResult simulate(UUID workspaceId, SimulationRequest request) {
        DataEntityEntity root = resolveRootEntity(workspaceId, request.targetReference());
        Map<UUID, List<DataRelationshipEntity>> outgoing = buildOutgoingMap(workspaceId);
        Map<UUID, Integer> depthByEntity = new HashMap<>();
        Map<UUID, String> classificationByEntity = new HashMap<>();
        Map<UUID, String> relationshipTypeByEntity = new HashMap<>();

        ArrayDeque<UUID> queue = new ArrayDeque<>();
        Set<UUID> visited = new HashSet<>();
        queue.add(root.getId());
        visited.add(root.getId());
        depthByEntity.put(root.getId(), 0);
        classificationByEntity.put(root.getId(), "DIRECT");
        relationshipTypeByEntity.put(root.getId(), "TARGET");

        while (!queue.isEmpty()) {
            UUID current = queue.removeFirst();
            int currentDepth = depthByEntity.getOrDefault(current, 0);
            for (DataRelationshipEntity relationship : outgoing.getOrDefault(current, List.of())) {
                UUID next = relationship.getTargetEntity().getId();
                if (visited.contains(next)) {
                    continue;
                }
                visited.add(next);
                queue.addLast(next);
                int nextDepth = currentDepth + 1;
                depthByEntity.put(next, nextDepth);
                classificationByEntity.put(next, classifyRelationship(relationship, nextDepth));
                relationshipTypeByEntity.put(next, relationship.getRelationshipType());
            }
        }

        List<SimulationResult.SimulationNode> affectedNodes = new ArrayList<>();
        for (UUID entityId : visited) {
            DataEntityEntity entity = dataEntityRepository.findById(entityId)
                    .orElseThrow(() -> new IllegalStateException("Entity missing during simulation"));
            int depth = depthByEntity.getOrDefault(entityId, 0);
            String classification = classificationByEntity.getOrDefault(entityId, "DIRECT");
            affectedNodes.add(new SimulationResult.SimulationNode(
                    entity.getId(),
                    entity.getName(),
                    entity.getEntityType(),
                    resolveSourceName(entity),
                    relationshipTypeByEntity.getOrDefault(entityId, "TARGET"),
                    classification,
                    entity.isHasOwnerReference(),
                    depth));
        }

        affectedNodes.sort(Comparator.comparingInt(SimulationResult.SimulationNode::depth)
                .thenComparing(SimulationResult.SimulationNode::entityName));

        int directCount = 0;
        int indirectCount = 0;
        int optionalCount = 0;
        int unknownCount = 0;
        int manualReviewCount = 0;
        for (SimulationResult.SimulationNode node : affectedNodes) {
            if (node.entityId().equals(root.getId())) {
                continue;
            }
            switch (node.classification()) {
                case "DIRECT" -> directCount++;
                case "INDIRECT" -> indirectCount++;
                case "OPTIONAL" -> optionalCount++;
                case "UNKNOWN" -> unknownCount++;
                default -> unknownCount++;
            }
            if ("UNKNOWN".equals(node.classification()) || !node.hasOwnerReference()) {
                manualReviewCount++;
            }
        }

        List<String> deletionSequence = affectedNodes.stream()
                .filter(node -> !node.entityId().equals(root.getId()))
                .sorted(Comparator.comparingInt(SimulationResult.SimulationNode::depth).reversed())
                .map(SimulationResult.SimulationNode::entityName)
                .toList();

        List<String> impactSummary = List.of(
                "Resolved target: " + request.targetReference(),
                "Affected records: " + affectedNodes.size(),
                "Direct dependencies: " + directCount,
                "Indirect dependencies: " + indirectCount,
                "Optional dependencies: " + optionalCount,
                "Unknown/deferred records: " + unknownCount,
                "Manual review required: " + manualReviewCount);

        return new SimulationResult(
                request.targetReference(),
                affectedNodes,
                directCount,
                indirectCount,
                optionalCount,
                unknownCount,
                manualReviewCount,
                deletionSequence,
                impactSummary);
    }

    private String resolveSourceName(DataEntityEntity entity) {
        DataSourceEntity source = entity.getDataSource();
        return source == null ? "Unassigned" : source.getName();
    }

    private String classifyRelationship(DataRelationshipEntity relationship, int depth) {
        String raw = relationship.getClassification();
        if (raw == null) {
            return depth > 1 ? "INDIRECT" : "DIRECT";
        }
        if ("OPTIONAL".equalsIgnoreCase(raw)) {
            return "OPTIONAL";
        }
        if ("UNKNOWN".equalsIgnoreCase(raw)) {
            return "UNKNOWN";
        }
        return depth > 1 ? "INDIRECT" : "DIRECT";
    }

    private Map<UUID, List<DataRelationshipEntity>> buildOutgoingMap(UUID workspaceId) {
        Map<UUID, List<DataRelationshipEntity>> map = new HashMap<>();
        for (DataRelationshipEntity relationship : dataRelationshipRepository.findByWorkspace_IdOrderByCreatedAtDesc(workspaceId)) {
            map.computeIfAbsent(relationship.getSourceEntity().getId(), key -> new ArrayList<>()).add(relationship);
        }
        return map;
    }

    private DataEntityEntity resolveRootEntity(UUID workspaceId, String targetReference) {
        Objects.requireNonNull(targetReference, "targetReference");
        String trimmed = targetReference.trim();
        UUID candidateId;
        try {
            candidateId = UUID.fromString(trimmed);
            return dataEntityRepository.findByWorkspace_IdAndId(workspaceId, candidateId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target entity not found"));
        } catch (IllegalArgumentException ignored) {
            return dataEntityRepository.findByWorkspace_IdAndExternalKey(workspaceId, trimmed)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target entity not found"));
        }
    }
}

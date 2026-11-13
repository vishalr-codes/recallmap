package com.recallmap.lineage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DataLineageGraphService {

    private final DataEntityRepository dataEntityRepository;
    private final DataRelationshipRepository dataRelationshipRepository;

    public DataLineageGraphService(
            DataEntityRepository dataEntityRepository,
            DataRelationshipRepository dataRelationshipRepository) {
        this.dataEntityRepository = dataEntityRepository;
        this.dataRelationshipRepository = dataRelationshipRepository;
    }

    public GraphResponse buildGraph(UUID workspaceId) {
        List<DataEntityEntity> entities = dataEntityRepository.findByWorkspace_IdOrderByNameAsc(workspaceId);
        List<DataRelationshipEntity> relationships = dataRelationshipRepository.findByWorkspace_IdOrderByCreatedAtDesc(workspaceId);

        Map<UUID, GraphNode> nodes = new LinkedHashMap<>();
        for (DataEntityEntity entity : entities) {
            GraphNode node = new GraphNode(
                    entity.getId(),
                    entity.getName(),
                    entity.getEntityType(),
                    entity.getDataSource() == null ? null : entity.getDataSource().getName(),
                    entity.isHasOwnerReference());
            nodes.put(entity.getId(), node);
        }

        List<GraphEdge> edges = new ArrayList<>();
        for (DataRelationshipEntity relationship : relationships) {
            GraphEdge edge = new GraphEdge(
                    relationship.getId(),
                    relationship.getSourceEntity().getId(),
                    relationship.getTargetEntity().getId(),
                    relationship.getRelationshipType(),
                    relationship.getClassification(),
                    relationship.getDescription());
            edges.add(edge);
        }

        return new GraphResponse(
                new ArrayList<>(nodes.values()),
                edges,
                detectCycleNodes(workspaceId),
                entities.size(),
                relationships.size());
    }

    public Set<UUID> findReachableEntities(UUID workspaceId, UUID rootEntityId) {
        Map<UUID, List<DataRelationshipEntity>> outgoing = new HashMap<>();
        for (DataRelationshipEntity relationship : dataRelationshipRepository.findByWorkspace_IdOrderByCreatedAtDesc(workspaceId)) {
            outgoing.computeIfAbsent(relationship.getSourceEntity().getId(), key -> new ArrayList<>())
                    .add(relationship);
        }

        Set<UUID> reachable = new HashSet<>();
        ArrayDeque<UUID> queue = new ArrayDeque<>();
        queue.add(rootEntityId);

        while (!queue.isEmpty()) {
            UUID current = queue.removeFirst();
            if (!reachable.add(current)) {
                continue;
            }
            for (DataRelationshipEntity relationship : outgoing.getOrDefault(current, List.of())) {
                queue.addLast(relationship.getTargetEntity().getId());
            }
        }

        reachable.remove(rootEntityId);
        return reachable;
    }

    public List<UUID> detectCycleNodes(UUID workspaceId) {
        List<DataEntityEntity> entities = dataEntityRepository.findByWorkspace_IdOrderByNameAsc(workspaceId);
        Map<UUID, List<UUID>> adjacency = new HashMap<>();
        for (DataEntityEntity entity : entities) {
            adjacency.put(entity.getId(), new ArrayList<>());
        }

        for (DataRelationshipEntity relationship : dataRelationshipRepository.findByWorkspace_IdOrderByCreatedAtDesc(workspaceId)) {
            adjacency.computeIfAbsent(relationship.getSourceEntity().getId(), key -> new ArrayList<>())
                    .add(relationship.getTargetEntity().getId());
        }

        Set<UUID> visited = new HashSet<>();
        Set<UUID> cycleNodes = new HashSet<>();
        for (UUID entityId : adjacency.keySet()) {
            visitForCycle(entityId, adjacency, visited, new ArrayList<>(), cycleNodes);
        }
        List<UUID> orderedNodes = new ArrayList<>(cycleNodes);
        orderedNodes.sort(UUID::compareTo);
        return orderedNodes;
    }

    private void visitForCycle(
            UUID current,
            Map<UUID, List<UUID>> adjacency,
            Set<UUID> visited,
            List<UUID> path,
            Set<UUID> cycleNodes) {
        int cycleStart = path.indexOf(current);
        if (cycleStart >= 0) {
            cycleNodes.addAll(path.subList(cycleStart, path.size()));
            return;
        }
        if (visited.contains(current)) {
            return;
        }

        path.add(current);
        for (UUID neighbor : adjacency.getOrDefault(current, List.of())) {
            if (!visited.contains(neighbor)) {
                visitForCycle(neighbor, adjacency, visited, path, cycleNodes);
            }
        }
        path.remove(path.size() - 1);
        visited.add(current);
    }

    public record GraphResponse(
            List<GraphNode> nodes,
            List<GraphEdge> edges,
            List<UUID> cycleNodes,
            int entityCount,
            int relationshipCount) {
    }

    public record GraphNode(
            UUID id,
            String label,
            String entityType,
            String sourceName,
            boolean hasOwnerReference) {
    }

    public record GraphEdge(
            UUID id,
            UUID sourceEntityId,
            UUID targetEntityId,
            String relationshipType,
            String classification,
            String description) {
    }
}

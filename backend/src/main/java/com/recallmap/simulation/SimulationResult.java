package com.recallmap.simulation;

import java.util.List;
import java.util.UUID;

public record SimulationResult(
        String targetReference,
        List<SimulationNode> affectedNodes,
        int directCount,
        int indirectCount,
        int optionalCount,
        int unknownCount,
        int manualReviewCount,
        List<String> proposedDeletionSequence,
        List<String> impactSummary) {

    public record SimulationNode(
            UUID entityId,
            String entityName,
            String entityType,
            String sourceName,
            String relationshipType,
            String classification,
            boolean hasOwnerReference,
            int depth) {
    }
}

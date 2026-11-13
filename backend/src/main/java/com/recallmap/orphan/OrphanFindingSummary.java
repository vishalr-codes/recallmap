package com.recallmap.orphan;

import java.util.UUID;

public record OrphanFindingSummary(
        UUID id,
        String findingType,
        String severity,
        String recordReference,
        String evidenceSummary,
        String status) {

    public static OrphanFindingSummary fromEntity(OrphanFindingEntity entity) {
        return new OrphanFindingSummary(
                entity.getId(),
                entity.getFindingType(),
                entity.getSeverity(),
                entity.getRecordReference(),
                entity.getEvidenceSummary(),
                entity.getStatus());
    }
}

package com.recallmap.deletion;

import java.util.UUID;

public record DeletionRequestSummary(
        UUID id,
        String targetType,
        String targetReference,
        String status,
        String idempotencyKey) {

    public static DeletionRequestSummary fromEntity(DeletionRequestEntity entity) {
        return new DeletionRequestSummary(
                entity.getId(),
                entity.getTargetType(),
                entity.getTargetReference(),
                entity.getStatus(),
                entity.getIdempotencyKey());
    }
}

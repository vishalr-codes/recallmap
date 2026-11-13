package com.recallmap.audit;

import java.util.UUID;

public record AuditEventSummary(
        UUID id,
        long sequenceNumber,
        String eventType,
        String previousHash,
        String eventHash,
        String payload) {

    public static AuditEventSummary fromEntity(AuditEventEntity entity) {
        return new AuditEventSummary(
                entity.getId(),
                entity.getSequenceNumber(),
                entity.getEventType(),
                entity.getPreviousHash(),
                entity.getEventHash(),
                entity.getPayload());
    }
}

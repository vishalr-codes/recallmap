package com.recallmap.verification;

import java.util.UUID;

public record VerificationResultSummary(
        UUID id,
        UUID deletionTargetId,
        String outcome,
        String evidenceHash,
        String evidenceSummary,
        String status) {

    public static VerificationResultSummary fromEntity(VerificationResultEntity entity) {
        return new VerificationResultSummary(
                entity.getId(),
                entity.getDeletionTarget().getId(),
                entity.getOutcome(),
                entity.getEvidenceHash(),
                entity.getEvidenceSummary(),
                entity.getOutcome());
    }
}

package com.recallmap.verification;

import com.recallmap.deletion.DeletionTargetEntity;
import com.recallmap.deletion.DeletionTargetRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VerificationReportService {

    private final VerificationResultRepository verificationResultRepository;
    private final DeletionTargetRepository deletionTargetRepository;

    public VerificationReportService(
            VerificationResultRepository verificationResultRepository,
            DeletionTargetRepository deletionTargetRepository) {
        this.verificationResultRepository = verificationResultRepository;
        this.deletionTargetRepository = deletionTargetRepository;
    }

    @Transactional
    public VerificationResultSummary recordVerification(UUID deletionTargetId, String outcome, String evidenceSummary) {
        DeletionTargetEntity target = deletionTargetRepository.findById(deletionTargetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deletion target not found"));

        UUID targetUuid = target.getId();
        String normalizedOutcome = outcome == null || outcome.isBlank() ? "INSUFFICIENT_EVIDENCE" : outcome.trim().toUpperCase();
        String normalizedSummary = evidenceSummary == null || evidenceSummary.isBlank() ? "Verification evidence was recorded without details." : evidenceSummary.trim();
        String evidenceHash = createEvidenceHash(targetUuid, normalizedOutcome, normalizedSummary);

        VerificationResultEntity result = new VerificationResultEntity();
        result.setDeletionTarget(target);
        result.setOutcome(normalizedOutcome);
        result.setEvidenceSummary(normalizedSummary);
        result.setEvidenceHash(evidenceHash);
        result.setVerifiedAt(OffsetDateTime.now());
        VerificationResultEntity saved = verificationResultRepository.save(result);
        return VerificationResultSummary.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public VerificationResultSummary latestForTarget(UUID deletionTargetId) {
        return verificationResultRepository.findByDeletionTarget_IdOrderByCreatedAtDesc(deletionTargetId).stream()
                .findFirst()
                .map(VerificationResultSummary::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No verification result found for target"));
    }

    private String createEvidenceHash(UUID targetId, String outcome, String evidenceSummary) {
        String raw = targetId + "|" + outcome + "|" + evidenceSummary;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte value : bytes) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}

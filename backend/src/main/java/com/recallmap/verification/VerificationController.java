package com.recallmap.verification;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class VerificationController {

    private final VerificationReportService verificationReportService;

    public VerificationController(VerificationReportService verificationReportService) {
        this.verificationReportService = verificationReportService;
    }

    @PostMapping("/verifications")
    public ResponseEntity<VerificationResultSummary> recordVerification(
            @RequestParam UUID deletionTargetId,
            @RequestParam(required = false, defaultValue = "VERIFIED") String outcome,
            @RequestParam(required = false, defaultValue = "Verification evidence recorded") String evidenceSummary) {
        return ResponseEntity.ok(verificationReportService.recordVerification(deletionTargetId, outcome, evidenceSummary));
    }

    @GetMapping("/verifications")
    public ResponseEntity<VerificationResultSummary> latestVerification(@RequestParam UUID deletionTargetId) {
        return ResponseEntity.ok(verificationReportService.latestForTarget(deletionTargetId));
    }
}

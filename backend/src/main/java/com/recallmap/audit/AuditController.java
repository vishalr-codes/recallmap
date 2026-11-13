package com.recallmap.audit;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AuditController {

    private final AuditLedgerService auditLedgerService;

    public AuditController(AuditLedgerService auditLedgerService) {
        this.auditLedgerService = auditLedgerService;
    }

    @GetMapping("/audit")
    public ResponseEntity<List<AuditEventSummary>> listAuditEvents(@RequestParam UUID workspaceId) {
        return ResponseEntity.ok(auditLedgerService.listEvents(workspaceId));
    }

    @PostMapping("/audit")
    public ResponseEntity<AuditEventSummary> appendAuditEvent(
            @RequestParam UUID workspaceId,
            @RequestParam(required = false) UUID actorId,
            @RequestParam String eventType,
            @RequestParam(required = false, defaultValue = "{}") String payload) {
        return ResponseEntity.ok(auditLedgerService.appendEvent(workspaceId, actorId, eventType, payload));
    }
}

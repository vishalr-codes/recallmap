package com.recallmap.orphan;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class OrphanController {

    private final OrphanDetectorService orphanDetectorService;

    public OrphanController(OrphanDetectorService orphanDetectorService) {
        this.orphanDetectorService = orphanDetectorService;
    }

    @GetMapping("/orphans")
    public ResponseEntity<List<OrphanFindingSummary>> listOrphans(@RequestParam UUID workspaceId) {
        return ResponseEntity.ok(orphanDetectorService.detect(workspaceId));
    }
}

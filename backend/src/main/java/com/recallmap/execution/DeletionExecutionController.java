package com.recallmap.execution;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DeletionExecutionController {

    private final DeletionExecutionService deletionExecutionService;

    public DeletionExecutionController(DeletionExecutionService deletionExecutionService) {
        this.deletionExecutionService = deletionExecutionService;
    }

    @PostMapping("/executions/{targetId}/run")
    public ResponseEntity<CompletableFuture<TargetExecutionResult>> runTargetExecution(@PathVariable UUID targetId) {
        return ResponseEntity.accepted().body(deletionExecutionService.executeAsync(targetId));
    }
}

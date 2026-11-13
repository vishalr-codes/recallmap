package com.recallmap.deletion;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.recallmap.auth.PrincipalUser;

@RestController
@RequestMapping("/api/v1")
public class DeletionRequestController {

    private final DeletionRequestService deletionRequestService;

    public DeletionRequestController(DeletionRequestService deletionRequestService) {
        this.deletionRequestService = deletionRequestService;
    }

    @PostMapping("/deletion-requests")
    public ResponseEntity<DeletionRequestSummary> createDeletionRequest(
            @AuthenticationPrincipal PrincipalUser principal,
            @RequestParam UUID workspaceId,
            @Valid @RequestBody CreateDeletionRequestRequest request) {
        return ResponseEntity.ok(deletionRequestService.createRequest(workspaceId, principal.getUserId(), request));
    }
}

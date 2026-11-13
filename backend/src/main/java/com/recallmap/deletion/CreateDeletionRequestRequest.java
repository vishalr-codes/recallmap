package com.recallmap.deletion;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

public record CreateDeletionRequestRequest(
        @NotBlank(message = "Target type is required") String targetType,
        @NotBlank(message = "Target reference is required") String targetReference,
        @NotBlank(message = "Idempotency key is required") String idempotencyKey,
        List<UUID> dataSourceIds) {
}

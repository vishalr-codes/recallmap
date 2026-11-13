package com.recallmap.execution;

import java.util.UUID;

public record TargetExecutionResult(
        UUID executionId,
        UUID targetId,
        String status,
        int attemptNumber,
        boolean success,
        String resultSummary,
        String errorCode) {
}

package com.recallmap.simulation;

import jakarta.validation.constraints.NotBlank;

public record SimulationRequest(
        @NotBlank(message = "Target reference is required") String targetReference,
        String targetType) {
}

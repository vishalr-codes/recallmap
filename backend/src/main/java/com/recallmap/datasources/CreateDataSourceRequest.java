package com.recallmap.datasources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDataSourceRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must be 120 characters or fewer")
        String name,
        @NotBlank(message = "Source type is required")
        @Size(max = 40, message = "Source type must be 40 characters or fewer")
        String sourceType,
        @NotBlank(message = "Connector key is required")
        @Size(max = 80, message = "Connector key must be 80 characters or fewer")
        String connectorKey,
        @Size(max = 500, message = "Description must be 500 characters or fewer")
        String description,
        Boolean demo) {
}

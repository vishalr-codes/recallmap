package com.recallmap.datasources;

import java.util.UUID;

public record DataSourceSummary(
        UUID id,
        String name,
        String sourceType,
        String connectorKey,
        String status,
        String description,
        boolean demo) {

    public static DataSourceSummary fromEntity(DataSourceEntity entity) {
        return new DataSourceSummary(
                entity.getId(),
                entity.getName(),
                entity.getSourceType(),
                entity.getConnectorKey(),
                entity.getStatus(),
                entity.getDescription(),
                entity.isDemo());
    }
}

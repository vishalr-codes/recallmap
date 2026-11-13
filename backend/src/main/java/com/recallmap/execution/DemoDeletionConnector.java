package com.recallmap.execution;

import com.recallmap.datasources.DataSourceEntity;
import com.recallmap.deletion.DeletionRequestEntity;
import com.recallmap.deletion.DeletionTargetEntity;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;

@Component
public class DemoDeletionConnector {

    public ExecutionOutcome execute(DeletionRequestEntity request, DeletionTargetEntity target, DataSourceEntity source) {
        if (source == null) {
            return new ExecutionOutcome(false, "DEMO_NO_SOURCE", "A data source is required before a demo deletion can be executed.");
        }

        if ("DEMO_FAILURE".equalsIgnoreCase(request.getTargetReference())) {
            return new ExecutionOutcome(false, "DEMO_CONNECTOR_FAILURE", "Demo connector intentionally rejected the target due to simulated failure policy.");
        }

        return new ExecutionOutcome(
                true,
                null,
                "Demo connector executed deletion for target '" + request.getTargetReference() + "' on source '" + source.getName() + "' at " + OffsetDateTime.now());
    }

    public record ExecutionOutcome(boolean success, String errorCode, String resultSummary) {
    }
}

package com.recallmap.execution;

import com.recallmap.datasources.DataSourceEntity;
import com.recallmap.deletion.DeletionRequestEntity;
import com.recallmap.deletion.DeletionRequestRepository;
import com.recallmap.deletion.DeletionTargetEntity;
import com.recallmap.deletion.DeletionTargetRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DeletionExecutionService {

    private static final int MAX_ATTEMPTS = 3;

    private final DeletionTargetRepository deletionTargetRepository;
    private final DeletionExecutionRepository deletionExecutionRepository;
    private final DeletionRequestRepository deletionRequestRepository;
    private final DemoDeletionConnector demoDeletionConnector;

    public DeletionExecutionService(
            DeletionTargetRepository deletionTargetRepository,
            DeletionExecutionRepository deletionExecutionRepository,
            DeletionRequestRepository deletionRequestRepository,
            DemoDeletionConnector demoDeletionConnector) {
        this.deletionTargetRepository = deletionTargetRepository;
        this.deletionExecutionRepository = deletionExecutionRepository;
        this.deletionRequestRepository = deletionRequestRepository;
        this.demoDeletionConnector = demoDeletionConnector;
    }

    @Async
    @Transactional
    public CompletableFuture<TargetExecutionResult> executeAsync(UUID deletionTargetId) {
        DeletionTargetEntity target = deletionTargetRepository.findById(deletionTargetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deletion target not found"));

        DeletionRequestEntity request = target.getDeletionRequest();
        List<DeletionExecutionEntity> previousExecutions = deletionExecutionRepository.findByDeletionTarget_IdOrderByAttemptNumberDesc(target.getId());
        int nextAttempt = previousExecutions.isEmpty() ? 1 : previousExecutions.get(0).getAttemptNumber() + 1;
        if (nextAttempt > MAX_ATTEMPTS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum retry attempts reached for target");
        }

        DeletionExecutionEntity execution = new DeletionExecutionEntity();
        execution.setDeletionTarget(target);
        execution.setAttemptNumber(nextAttempt);
        execution.setStatus("RUNNING");
        execution.setIdempotencyKey("target-" + target.getId() + "-attempt-" + nextAttempt);
        execution.setStartedAt(OffsetDateTime.now());
        DeletionExecutionEntity savedExecution = deletionExecutionRepository.save(execution);

        target.setStatus("RUNNING");
        request.setStatus("RUNNING");
        deletionTargetRepository.save(target);
        deletionRequestRepository.save(request);

        DataSourceEntity source = target.getDataSource();
        DemoDeletionConnector.ExecutionOutcome outcome = demoDeletionConnector.execute(request, target, source);

        savedExecution.setCompletedAt(OffsetDateTime.now());
        savedExecution.setStatus(outcome.success() ? "SUCCEEDED" : "FAILED");
        savedExecution.setResultSummary(outcome.resultSummary());
        savedExecution.setErrorCode(outcome.errorCode());
        deletionExecutionRepository.save(savedExecution);

        if (outcome.success()) {
            target.setStatus("SUCCEEDED");
            deletionTargetRepository.save(target);
            updateRequestStatus(request);
            return CompletableFuture.completedFuture(new TargetExecutionResult(
                    savedExecution.getId(),
                    target.getId(),
                    "SUCCEEDED",
                    nextAttempt,
                    true,
                    outcome.resultSummary(),
                    null));
        }

        target.setStatus("FAILED");
        deletionTargetRepository.save(target);
        updateRequestStatus(request);
        return CompletableFuture.completedFuture(new TargetExecutionResult(
                savedExecution.getId(),
                target.getId(),
                "FAILED",
                nextAttempt,
                false,
                outcome.resultSummary(),
                outcome.errorCode()));
    }

    @Transactional(readOnly = true)
    public Optional<DeletionExecutionEntity> getLatestExecution(UUID deletionTargetId) {
        return deletionExecutionRepository.findByDeletionTarget_IdOrderByAttemptNumberDesc(deletionTargetId).stream()
                .findFirst();
    }

    private void updateRequestStatus(DeletionRequestEntity request) {
        List<DeletionTargetEntity> targets = deletionTargetRepository.findByDeletionRequest_IdOrderByCreatedAtAsc(request.getId());
        if (targets.stream().allMatch(target -> "SUCCEEDED".equals(target.getStatus()))) {
            request.setStatus("COMPLETE");
        } else if (targets.stream().anyMatch(target -> "FAILED".equals(target.getStatus()))) {
            request.setStatus("FAILED");
        } else {
            request.setStatus("PARTIAL");
        }
        deletionRequestRepository.save(request);
    }
}

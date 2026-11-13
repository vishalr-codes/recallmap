package com.recallmap.deletion;

import com.recallmap.datasources.DataSourceEntity;
import com.recallmap.datasources.DataSourceRepository;
import com.recallmap.organizations.WorkspaceEntity;
import com.recallmap.organizations.WorkspaceRepository;
import com.recallmap.users.UserEntity;
import com.recallmap.users.UserRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DeletionRequestService {

    private final DeletionRequestRepository deletionRequestRepository;
    private final DeletionTargetRepository deletionTargetRepository;
    private final WorkspaceRepository workspaceRepository;
    private final DataSourceRepository dataSourceRepository;
    private final UserRepository userRepository;

    public DeletionRequestService(
            DeletionRequestRepository deletionRequestRepository,
            DeletionTargetRepository deletionTargetRepository,
            WorkspaceRepository workspaceRepository,
            DataSourceRepository dataSourceRepository,
            UserRepository userRepository) {
        this.deletionRequestRepository = deletionRequestRepository;
        this.deletionTargetRepository = deletionTargetRepository;
        this.workspaceRepository = workspaceRepository;
        this.dataSourceRepository = dataSourceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DeletionRequestSummary createRequest(UUID workspaceId, UUID userId, CreateDeletionRequestRequest request) {
        Objects.requireNonNull(workspaceId, "workspaceId");
        Objects.requireNonNull(userId, "userId");
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request payload is required");
        }

        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String key = request.idempotencyKey() == null ? null : request.idempotencyKey().trim();
        if (key == null || key.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency key is required");
        }

        Optional<DeletionRequestEntity> existing = deletionRequestRepository.findByWorkspace_IdAndIdempotencyKey(workspaceId, key);
        if (existing.isPresent()) {
            return DeletionRequestSummary.fromEntity(existing.get());
        }

        DeletionRequestEntity deletionRequest = new DeletionRequestEntity();
        deletionRequest.setWorkspace(workspace);
        deletionRequest.setRequestedBy(user);
        deletionRequest.setTargetType(request.targetType().trim());
        deletionRequest.setTargetReference(request.targetReference().trim());
        deletionRequest.setStatus("REQUESTED");
        deletionRequest.setIdempotencyKey(key);
        DeletionRequestEntity savedRequest = deletionRequestRepository.save(deletionRequest);

        List<UUID> sourceIds = request.dataSourceIds() == null ? List.of() : request.dataSourceIds();
        List<DataSourceEntity> targets = sourceIds.isEmpty()
                ? dataSourceRepository.findByWorkspace_IdOrderByNameAsc(workspaceId)
                : sourceIds.stream()
                        .map(id -> dataSourceRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Data source not found: " + id)))
                        .filter(source -> source.getWorkspace().getId().equals(workspaceId))
                        .toList();

        List<DeletionTargetEntity> createdTargets = new ArrayList<>();
        for (DataSourceEntity source : targets) {
            DeletionTargetEntity target = new DeletionTargetEntity();
            target.setWorkspace(workspace);
            target.setDeletionRequest(savedRequest);
            target.setDataSource(source);
            target.setStatus("PENDING");
            target.setRequired(true);
            createdTargets.add(deletionTargetRepository.save(target));
        }

        if (createdTargets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one data source target is required");
        }

        return DeletionRequestSummary.fromEntity(savedRequest);
    }
}

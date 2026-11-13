package com.recallmap.datasources;

import com.recallmap.organizations.WorkspaceEntity;
import com.recallmap.organizations.WorkspaceRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class DataSourceController {

    private final DataSourceRepository dataSourceRepository;
    private final WorkspaceRepository workspaceRepository;

    public DataSourceController(DataSourceRepository dataSourceRepository, WorkspaceRepository workspaceRepository) {
        this.dataSourceRepository = dataSourceRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @GetMapping("/data-sources")
    @Transactional(readOnly = true)
    public List<DataSourceSummary> listDataSources(@RequestParam UUID workspaceId) {
        workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));
        return dataSourceRepository.findByWorkspace_IdOrderByNameAsc(workspaceId).stream()
                .map(DataSourceSummary::fromEntity)
                .toList();
    }

    @PostMapping("/data-sources")
    @Transactional
    public ResponseEntity<DataSourceSummary> createDataSource(
            @RequestParam UUID workspaceId,
            @Valid @RequestBody CreateDataSourceRequest request) {
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        if (dataSourceRepository.existsByWorkspace_IdAndNameIgnoreCase(workspaceId, request.name().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A data source with that name already exists in the workspace");
        }

        DataSourceEntity source = new DataSourceEntity();
        source.setWorkspace(workspace);
        source.setName(request.name().trim());
        source.setSourceType(request.sourceType().trim());
        source.setConnectorKey(request.connectorKey().trim());
        source.setDescription(request.description() == null ? null : request.description().trim());
        source.setDemo(Boolean.TRUE.equals(request.demo()));
        source.setStatus("REGISTERED");
        DataSourceEntity saved = dataSourceRepository.save(source);

        return ResponseEntity.status(HttpStatus.CREATED).body(DataSourceSummary.fromEntity(saved));
    }
}

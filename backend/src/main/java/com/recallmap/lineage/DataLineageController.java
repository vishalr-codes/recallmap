package com.recallmap.lineage;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lineage")
public class DataLineageController {

    private final DataLineageGraphService dataLineageGraphService;

    public DataLineageController(DataLineageGraphService dataLineageGraphService) {
        this.dataLineageGraphService = dataLineageGraphService;
    }

    @GetMapping("/graph")
    public ResponseEntity<DataLineageGraphService.GraphResponse> getGraph(@RequestParam UUID workspaceId) {
        return ResponseEntity.ok(dataLineageGraphService.buildGraph(workspaceId));
    }
}

package com.recallmap.simulation;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SimulationController {

    private final DeletionImpactSimulationService simulationService;

    public SimulationController(DeletionImpactSimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("/simulations")
    public ResponseEntity<SimulationResult> simulate(
            @RequestParam UUID workspaceId,
            @Valid @RequestParam String targetReference) {
        SimulationRequest request = new SimulationRequest(targetReference, "ENTITY");
        return ResponseEntity.ok(simulationService.simulate(workspaceId, request));
    }
}

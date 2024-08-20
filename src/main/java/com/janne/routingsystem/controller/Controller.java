package com.janne.routingsystem.controller;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.janne.routingsystem.model.CoordinateDto;
import com.janne.routingsystem.model.MemoryStats;
import com.janne.routingsystem.model.incoming.FleetInstructionsRequest;
import com.janne.routingsystem.model.incoming.RouteResponse;
import com.janne.routingsystem.service.GraphHopperService;
import com.janne.routingsystem.service.RoutingService;
import com.janne.routingsystem.service.scheduling.SchedulingService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
public class Controller {

    private final GraphHopperService graphHopperService;
    private final RoutingService routingService;
    private final SchedulingService schedulingService;

    @GetMapping("/route")
    public ResponseEntity<RouteResponse> route(@RequestParam CoordinateDto from, @RequestParam CoordinateDto to) {
        RouteResponse result = graphHopperService.calculateRoute(from, to);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/fleetInstructions")
    public ResponseEntity<String> vehicleRoutingProblemSolver(
            @RequestBody FleetInstructionsRequest fleetInstructionsRequest
    ) {
        return ResponseEntity.accepted().body(schedulingService.scheduleTask(fleetInstructionsRequest));
    }

    @GetMapping("/fleetInstructions/{uuid}")
    public ResponseEntity<VehicleRoutingProblemSolution> getVehicleRoutingSolution(@PathVariable("uuid") String uuid) {
        if (!schedulingService.doesTaskExist(uuid)) {
            return ResponseEntity.notFound().build();
        }

        if (!schedulingService.isFinished(uuid)) {
            return ResponseEntity.accepted().body(null);
        }
        return ResponseEntity.ok(schedulingService.getResult(uuid));
    }

    @GetMapping("memory-status")
    public MemoryStats getMemoryStatistics() {
        MemoryStats stats = new MemoryStats();
        stats.setHeapSize(Runtime.getRuntime().totalMemory());
        stats.setHeapMaxSize(Runtime.getRuntime().maxMemory());
        stats.setHeapFreeSize(Runtime.getRuntime().freeMemory());
        System.out.println(stats.toString());
        return stats;
    }
}

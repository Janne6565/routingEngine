package com.janne.routingsystem.controller;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.janne.routingsystem.model.Coordinate;
import com.janne.routingsystem.model.incoming.FleetInstructionsRequest;
import com.janne.routingsystem.model.incoming.RouteResponse;
import com.janne.routingsystem.service.GraphHopperService;
import com.janne.routingsystem.service.RoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class Controller {

    private final GraphHopperService graphHopperService;
    private final RoutingService routingService;

    @GetMapping("/route")
    public ResponseEntity<RouteResponse> route(@RequestParam Coordinate from, @RequestParam Coordinate to) {
        RouteResponse result = graphHopperService.calculateRoute(from, to);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/fleetInstructions")
    public ResponseEntity<VehicleRoutingProblemSolution> vehicleRoutingProblemSolver(
            @RequestBody FleetInstructionsRequest fleetInstructionsRequest
    ) {
        return ResponseEntity.ok(routingService.calculateBestSolution(fleetInstructionsRequest.getVehicles(), fleetInstructionsRequest.getJobs()));
    }
}

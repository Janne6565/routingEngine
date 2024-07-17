package com.janne.routingsystem.service;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import com.janne.routingsystem.graphhopper.CustomDistanceCalculator;
import com.janne.routingsystem.model.Coordinate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class RoutingService {

    private final CustomDistanceCalculator customDistanceCalculator;

    public VehicleRoutingProblemSolution calculateBestSolution(Coordinate[] startPositions, Coordinate[] jobPositions) {
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("defaultCarType");
        VehicleType defaultCarType = vehicleTypeBuilder.build();


        VehicleRoutingProblem.Builder vehicleRoutingProblem = VehicleRoutingProblem.Builder.newInstance();


        int vehicleCounter = 0;
        for (Coordinate startPosition : startPositions) {
            vehicleRoutingProblem.addVehicle(VehicleImpl.Builder.newInstance("vehicle" + vehicleCounter)
                    .setStartLocation(startPosition.toLocation())
                    .setType(defaultCarType)
                    .build());
            vehicleCounter += 1;
        }

        int jobCounter = 0;
        for (Coordinate jobPosition : jobPositions) {
            vehicleRoutingProblem.addJob(com.graphhopper.jsprit.core.problem.job.Service.Builder.newInstance("job" + jobCounter)
                    .setLocation(jobPosition.toLocation())
                    .build());
            jobCounter += 1;
        }

        VehicleRoutingProblem problem = vehicleRoutingProblem.build();
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        VehicleRoutingProblemSolution vehicleRoutingProblemSolution = Solutions.bestOf(solutions);

        return vehicleRoutingProblemSolution;
    }
}

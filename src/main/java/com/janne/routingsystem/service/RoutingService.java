package com.janne.routingsystem.service;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import com.janne.routingsystem.graphhopper.CustomDistanceCalculator;
import com.janne.routingsystem.model.Coordinate;
import com.janne.routingsystem.model.dto.JobDto;
import com.janne.routingsystem.model.dto.VehicleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class RoutingService {

    private final CustomDistanceCalculator customDistanceCalculator;

    @Cacheable(value = "solutions", key = "#vehicleDtos.toString() + '-' + #jobPositions.toString()")
    public VehicleRoutingProblemSolution calculateBestSolution(VehicleDto[] vehicleDtos, JobDto[] jobPositions) {
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("defaultCarType");
        VehicleType defaultCarType = vehicleTypeBuilder.build();

        VehicleRoutingProblem.Builder vehicleRoutingProblem = VehicleRoutingProblem.Builder.newInstance();
        vehicleRoutingProblem.setRoutingCost(customDistanceCalculator);
        vehicleRoutingProblem.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

        for (VehicleDto vehicleDto : vehicleDtos) {
            Coordinate startPosition = vehicleDto.getPosition();
            vehicleRoutingProblem.addVehicle(VehicleImpl.Builder.newInstance(vehicleDto.getId())
                    .setStartLocation(startPosition.toLocation())
                    .setEarliestStart(vehicleDto.getEarliestTime())
                    .setLatestArrival(vehicleDto.getLatestTime())
                    .setType(defaultCarType)
                    .build());
        }

        for (JobDto jobDto : jobPositions) {
            Coordinate jobPosition = jobDto.getPosition();
            vehicleRoutingProblem.addJob(com.graphhopper.jsprit.core.problem.job.Service.Builder.newInstance(jobDto.getId())
                    .setLocation(jobPosition.toLocation())
                    .setName(jobDto.getId())
                    .setServiceTime(jobDto.getServiceTime())
                    .setTimeWindow(new TimeWindow(jobDto.getEarliestTime(), jobDto.getLatestTime()))
                    .build());
        }

        VehicleRoutingProblem problem = vehicleRoutingProblem.build();
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);
        return bestSolution;
    }
}

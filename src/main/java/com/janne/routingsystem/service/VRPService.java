package com.janne.routingsystem.service;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.janne.routingsystem.graphhopper.CustomRoutingCostTransportCalculator;
import com.janne.routingsystem.model.dto.JobDto;
import com.janne.routingsystem.model.dto.VehicleDto;
import com.janne.routingsystem.service.routingService.RoutingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VRPService {

    private final RoutingService routingService;
    private final CustomRoutingCostTransportCalculator customRoutingCostTransportCalculator;
    private final Logger logger = LoggerFactory.getLogger(VRPService.class);

    public VehicleRoutingProblemSolution calculateBestSolution(VehicleDto[] vehicleDtos, JobDto[] jobPositions, String key, int iterations, String previousSolutionKey) {
        VehicleType defaultCarType = VehicleTypeImpl.Builder.newInstance("defaultCarType").setMaxVelocity(0.7).build();
        List<Location> locations = new ArrayList<>();

        List<Vehicle> vehicles = new ArrayList<>();
        for (VehicleDto vehicleDto : vehicleDtos) {
            Location location = vehicleDto.getPosition().toLocation();
            vehicles.add(VehicleImpl.Builder.newInstance(vehicleDto.getId())
                    .setStartLocation(location)
                    .setEarliestStart(vehicleDto.getEarliestTime())
                    .setLatestArrival(vehicleDto.getLatestTime())
                    .setReturnToDepot(true)
                    .setType(defaultCarType)
                    .build());

            locations.add(location);
        }

        List<Job> jobs = new ArrayList<>();
        for (JobDto jobDto : jobPositions) {
            Location location = jobDto.getPosition().toLocation();
            jobs.add(com.graphhopper.jsprit.core.problem.job.Service.Builder.newInstance(jobDto.getId())
                    .setLocation(location)
                    .setServiceTime(jobDto.getServiceTime())
                    .setTimeWindow(new TimeWindow(jobDto.getEarliestTime(), jobDto.getLatestTime()))
                    .setName(jobDto.getId())
                    .setUserData(jobDto.getServiceTime())
                    .build());

            locations.add(location);
        }
        VehicleRoutingTransportCostsMatrix transportCostsMatrix = previousSolutionKey != null ? routingService.buildDistanceMatrix(locations.toArray(Location[]::new), false, previousSolutionKey) : routingService.buildDistanceMatrix(locations.toArray(Location[]::new), false, key);

        VehicleRoutingProblem problem = VehicleRoutingProblem.Builder.newInstance()
                .addAllJobs(jobs)
                .addAllVehicles(vehicles)
                .setRoutingCost(transportCostsMatrix)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .build();

        Jsprit.Builder algorithmBuilder = Jsprit.Builder.newInstance(problem);
        algorithmBuilder.setProperty(Jsprit.Parameter.THREADS, "8");
        VehicleRoutingAlgorithm algorithm = algorithmBuilder.buildAlgorithm();

        algorithm.setMaxIterations(40);
        algorithm.setMaxIterations(iterations);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        return Solutions.bestOf(solutions);
    }

    private static Location getRandomLocation() {
        return Location.newInstance(Math.random() * 1000, Math.random() * 1000);
    }
}

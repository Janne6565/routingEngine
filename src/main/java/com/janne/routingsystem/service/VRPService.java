package com.janne.routingsystem.service;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Break;
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
import com.janne.routingsystem.model.CoordinateDto;
import com.janne.routingsystem.model.dto.JobDto;
import com.janne.routingsystem.model.dto.VehicleDto;
import com.janne.routingsystem.model.incoming.RouteResponse;
import com.janne.routingsystem.service.routingService.RoutingService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class VRPService {

    private final RoutingService routingService;
    private final CustomRoutingCostTransportCalculator customRoutingCostTransportCalculator;
    private final Logger logger = LoggerFactory.getLogger(VRPService.class);
    private final Map<String, VehicleRoutingTransportCostsMatrix> cachedDistanceMatrices = new ConcurrentHashMap<>();

    @Cacheable(value = "solutions", key = "#key")
    public VehicleRoutingProblemSolution calculateBestSolution(VehicleDto[] vehicleDtos, JobDto[] jobPositions, String key, VehicleRoutingProblemSolution previousSolution, String previousSolutionKey) {
        VehicleType defaultCarType = VehicleTypeImpl.Builder.newInstance("defaultCarType").build();
        List<Location> locations = new ArrayList<>();

        List<Vehicle> vehicles = new ArrayList<>();
        for (VehicleDto vehicleDto : vehicleDtos) {
            Location location = vehicleDto.getPosition().toLocation();
            vehicles.add(VehicleImpl.Builder.newInstance(vehicleDto.getId())
                    .setStartLocation(location)
                    .setEarliestStart(vehicleDto.getEarliestTime())
                    .setLatestArrival(vehicleDto.getLatestTime())
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
                    .build());

            locations.add(location);
        }
        System.out.println(previousSolutionKey != null ? "Previous solution key: " + previousSolutionKey : "No previous solution key");
        VehicleRoutingTransportCostsMatrix transportCostsMatrix = previousSolutionKey != null ? routingService.buildDistanceMatrix(locations.toArray(Location[]::new), false, previousSolutionKey) : routingService.buildDistanceMatrix(locations.toArray(Location[]::new), false, key);

        VehicleRoutingProblem problem = VehicleRoutingProblem.Builder.newInstance()
                .addAllJobs(jobs)
                .addAllVehicles(vehicles)
                .setRoutingCost(transportCostsMatrix)
                .build();

        Jsprit.Builder algorithmBuilder = Jsprit.Builder.newInstance(problem);
        algorithmBuilder.setProperty(Jsprit.Parameter.THREADS, "8");
        VehicleRoutingAlgorithm algorithm = algorithmBuilder.buildAlgorithm();

        if (previousSolution != null) {
            algorithm.addInitialSolution(previousSolution);
        }

        algorithm.setMaxIterations(40);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        return Solutions.bestOf(solutions);
    }



    private static Location getRandomLocation() {
        return Location.newInstance(Math.random() * 1000, Math.random() * 1000);
    }
}

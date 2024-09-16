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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class VRPService {

    private final RoutingService routingService;
    private static final Logger logger = LoggerFactory.getLogger(VRPService.class);

    public VehicleRoutingProblemSolution calculateBestSolution(VehicleDto[] vehicleDtos, JobDto[] jobPositions, int iterations) {
        VehicleType defaultCarType = VehicleTypeImpl.Builder.newInstance("defaultCarType").setMaxVelocity(0.7).build();
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

        VehicleRoutingTransportCostsMatrix transportCostsMatrix = buildDistanceMatrixForProblem(locations.toArray(Location[]::new), false);

        VehicleRoutingProblem problem = VehicleRoutingProblem.Builder.newInstance()
                .addAllJobs(jobs)
                .addAllVehicles(vehicles)
                .setRoutingCost(transportCostsMatrix)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .build();

        Jsprit.Builder algorithmBuilder = Jsprit.Builder.newInstance(problem);
        algorithmBuilder.setProperty(Jsprit.Parameter.THREADS, "8");
        VehicleRoutingAlgorithm algorithm = algorithmBuilder.buildAlgorithm();
        algorithm.setMaxIterations(iterations);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        return Solutions.bestOf(solutions);
    }

    private VehicleRoutingTransportCostsMatrix buildDistanceMatrixForProblem(Location[] locations, boolean isSymmetric) {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        VehicleRoutingTransportCostsMatrix.Builder builder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(isSymmetric);
        Set<Pair<String, String>> visited = ConcurrentHashMap.newKeySet();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        int counterStarted = 0;

        for (int outerIndex = 0; outerIndex < locations.length; outerIndex++) {
            Location location1 = locations[outerIndex];

            for (int innerIndex = isSymmetric ? outerIndex + 1 : 0; innerIndex < locations.length; innerIndex++) {
                if (outerIndex == innerIndex) {
                    continue;
                }

                Location location2 = locations[innerIndex];
                Pair<String, String> currentRoute = new Pair<>(location1.toString(), location2.toString());

                if (visited.contains(currentRoute) || (isSymmetric && visited.contains(new Pair<>(location2.toString(), location1.toString())))) {
                    continue;
                }

                visited.add(currentRoute);

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        RouteResponse route = routingService.calculateRoute(CoordinateDto.fromLocation(location1), CoordinateDto.fromLocation(location2));
                        RouteResponse.Path path = route.getPaths().getFirst();
                        synchronized (builder) {
                            builder.addTransportDistance(location1.getId(), location2.getId(), path.getDistance());
                            builder.addTransportTime(location1.getId(), location2.getId(), (double) path.getTime() / 1000 / 60);
                        }
                    } catch (Exception e) {
                        logger.error("Error processing route between {} and {}", location1.getId(), location2.getId(), e);
                    }
                }, executorService);

                futures.add(future);
            }
            logger.info("Started {}/{} {}%", ++counterStarted, locations.length, counterStarted/locations.length * 100);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();

        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error("ExecutorService interrupted during shutdown", e);
        }

        return builder.build();
    }

    private static Location getRandomLocation() {
        return Location.newInstance(Math.random() * 1000, Math.random() * 1000);
    }
}

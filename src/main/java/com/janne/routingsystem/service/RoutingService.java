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
import com.janne.routingsystem.model.CoordinateDto;
import com.janne.routingsystem.model.dto.JobDto;
import com.janne.routingsystem.model.dto.VehicleDto;
import com.janne.routingsystem.model.incoming.RouteResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RoutingService {

    private final GraphHopperService graphHopperService;
    private final CustomRoutingCostTransportCalculator customRoutingCostTransportCalculator;
    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);

    @Cacheable(value = "solutions", key = "#key")
    public VehicleRoutingProblemSolution calculateBestSolution(VehicleDto[] vehicleDtos, JobDto[] jobPositions, String key) {
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

        VehicleRoutingTransportCostsMatrix transportCostsMatrix = buildDistanceMatrixForProblem(locations.toArray(Location[]::new), false);

        VehicleRoutingProblem problem = VehicleRoutingProblem.Builder.newInstance()
                .addAllJobs(jobs)
                .addAllVehicles(vehicles)
                .setRoutingCost(transportCostsMatrix)
                .build();

        Jsprit.Builder algorithmBuilder = Jsprit.Builder.newInstance(problem);
        algorithmBuilder.setProperty(Jsprit.Parameter.THREADS, "8");
        VehicleRoutingAlgorithm algorithm = algorithmBuilder.buildAlgorithm();
        algorithm.setMaxIterations(40);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        return Solutions.bestOf(solutions);
    }

    private VehicleRoutingTransportCostsMatrix buildDistanceMatrixForProblem(Location[] locations, boolean makeFaster) {
        VehicleRoutingTransportCostsMatrix.Builder builder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(makeFaster);
        Set<Pair<String, String>> visited = new HashSet<>();
        int done = 0;
        for (int outerIndex = 0; outerIndex < locations.length; outerIndex++) {
            Location location1 = locations[outerIndex];
            for (int innerIndex = makeFaster ? outerIndex + 1 : 0; innerIndex < locations.length; innerIndex++) {
                if (outerIndex == innerIndex) {
                    continue;
                }

                Location location2 = locations[innerIndex];

                Pair<String, String> currentRoute = new Pair<>(location1.toString(), location2.toString());
                if (visited.contains(currentRoute) || (makeFaster && visited.contains(new Pair<>(location2.toString(), location1.toString())))) {
                    continue;
                }
                visited.add(currentRoute);

                RouteResponse route = graphHopperService.calculateRoute(CoordinateDto.fromLocation(location1), CoordinateDto.fromLocation(location2));
                RouteResponse.Path path = route.getPaths().getFirst();
                builder.addTransportDistance(location1.getId(), location2.getId(), path.getDistance());
                builder.addTransportTime(location1.getId(), location2.getId(), path.getTime() / 1000 / 60);
            }
            done += 1;
            logger.info("Progress: {}% ({}/{})", (((float) done) / locations.length) * 100, done, locations.length);
        }

        return builder.build();
    }


    private static Location getRandomLocation() {
        return Location.newInstance(Math.random() * 1000, Math.random() * 1000);
    }
}

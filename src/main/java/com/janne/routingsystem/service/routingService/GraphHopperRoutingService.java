package com.janne.routingsystem.service.routingService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.janne.routingsystem.model.CoordinateDto;
import com.janne.routingsystem.model.incoming.RouteResponse;
import com.janne.routingsystem.model.outgoing.RouteRequest;
import com.janne.routingsystem.service.VRPService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.jackson.EndpointObjectMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class GraphHopperRoutingService implements RoutingService {

    private final boolean DEBUG = false;

    private static final Logger log = LoggerFactory.getLogger(GraphHopperRoutingService.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final EndpointObjectMapper endpointObjectMapper;
    private final ObjectMapper jacksonObjectMapper;
    private int counter = 0;
    private Map<CoordinateDto, Integer> countsUsedAsNode = new HashMap<>();

    /**
     * Calculates the optimal route to travel from coordinateA to coordinateB
     *
     * @param coordinateDtoA Starting Coordinate
     * @param coordinateDtoB Destination Coordinate
     * @return RouteResponse object
     */
    @Cacheable(value = "routes", key = "#coordinateDtoA.toString() + '-' + #coordinateDtoB.toString()")
    public RouteResponse calculateRoute(CoordinateDto coordinateDtoA, CoordinateDto coordinateDtoB) {
        if (DEBUG) {
            countsUsedAsNode.put(coordinateDtoA, countsUsedAsNode.getOrDefault(coordinateDtoA, 0) + 1);
            if (countsUsedAsNode.getOrDefault(coordinateDtoA, 0) % 100 == 0) {
                log.info("Visited node {} {} times", coordinateDtoA.buildToJson(), countsUsedAsNode.get(coordinateDtoA));
            }
            counter++;
            if (counter % 10000 == 0) {
                log.info("Counter {}", counter);
            }
        }

        try {
            RouteRequest routeRequest = RouteRequest.builder()
                    .points(new CoordinateDto[]{coordinateDtoA, coordinateDtoB})
                    .build();
            Mono<String> routeResponseMono = webClient.post()
                    .uri("/route?key=")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(routeRequest.buildJsonString())
                    .retrieve()
                    .bodyToMono(String.class);

            String routeResponse = routeResponseMono.block();
            return objectMapper.readValue(routeResponse, RouteResponse.class);
        } catch (Exception e) {
            log.error("Error while calculating route for {} to {}", coordinateDtoA.buildToJson(), coordinateDtoB.buildToJson());
            log.error("Error message: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Cacheable(value = "distanceMatrix", key = "#key")
    @Override
    public VehicleRoutingTransportCostsMatrix buildDistanceMatrix(Location[] locations, boolean isSymmetric, String key) {
        System.out.println(STR."Key: \{key}");
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        VehicleRoutingTransportCostsMatrix.Builder builder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(isSymmetric);
        Set<Pair<String, String>> visited = ConcurrentHashMap.newKeySet();  // Thread-safe set
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
                        RouteResponse route = calculateRoute(CoordinateDto.fromLocation(location1), CoordinateDto.fromLocation(location2));
                        RouteResponse.Path path = route.getPaths().getFirst();
                        synchronized (builder) {
                            builder.addTransportDistance(location1.getId(), location2.getId(), path.getDistance());
                            builder.addTransportTime(location1.getId(), location2.getId(), (double) path.getTime() / 1000 / 60);
                        }
                    } catch (Exception e) {
                        log.error("Error processing route between {} and {}", location1.getId(), location2.getId(), e);
                    }
                }, executorService);

                futures.add(future);
            }
            log.info("Started {}/{} {}%", ++counterStarted, locations.length, counterStarted/locations.length * 100);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();

        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.error("ExecutorService interrupted during shutdown", e);
        }

        return builder.build();
    }}

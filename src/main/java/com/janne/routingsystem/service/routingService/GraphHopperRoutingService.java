package com.janne.routingsystem.service.routingService;

import com.janne.routingsystem.model.CoordinateDto;
import com.janne.routingsystem.model.incoming.RouteResponse;
import com.janne.routingsystem.model.outgoing.RouteRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GraphHopperRoutingService implements RoutingService {

    private final boolean DEBUG = false;

    private static final Logger log = LoggerFactory.getLogger(GraphHopperRoutingService.class);
    private final WebClient webClient;
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
            Mono<RouteResponse> routeResponseMono = webClient.post()
                    .uri("/route?key=")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(routeRequest.buildJsonString())
                    .retrieve()
                    .bodyToMono(RouteResponse.class);

            return routeResponseMono.block();
        } catch (Exception e) {
            log.error("Error while calculating route for {} to {}", coordinateDtoA.buildToJson(), coordinateDtoB.buildToJson());
            log.error("Error message: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

package com.janne.routingsystem.service;

import com.janne.routingsystem.model.Coordinate;
import com.janne.routingsystem.model.incoming.RouteResponse;
import com.janne.routingsystem.model.outgoing.RouteRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GraphHopperService {

    private static final Logger log = LoggerFactory.getLogger(GraphHopperService.class);
    private final WebClient webClient;

    /**
     * Calculates the optimal route to travel from coordinateA to coordinateB
     *
     * @param coordinateA Starting Coordinate
     * @param coordinateB Destination Coordinate
     * @return RouteResponse object
     */
    @Cacheable(value = "routes", key = "#coordinateA.toString() + '-' + #coordinateB.toString()")
    public RouteResponse calculateRoute(Coordinate coordinateA, Coordinate coordinateB) {
        try {
            RouteRequest routeRequest = RouteRequest.builder()
                    .points(new Coordinate[]{coordinateA, coordinateB})
                    .build();
            System.out.println(routeRequest.buildJsonString());
            Mono<RouteResponse> routeResponseMono = webClient.post()
                    .uri("/route?key=")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(routeRequest.buildJsonString())
                    .retrieve()
                    .bodyToMono(RouteResponse.class);

            return routeResponseMono.block();
        } catch (Exception e) {
            log.error("Error while calculating route for {} to {}", coordinateA.buildToJson(), coordinateB.buildToJson());
            throw new RuntimeException(e);
        }
    }
}

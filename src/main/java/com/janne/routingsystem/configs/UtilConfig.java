package com.janne.routingsystem.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.janne.routingsystem.service.routingService.GraphHopperRoutingService;
import com.janne.routingsystem.service.routingService.RoutingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class UtilConfig {
    @Bean
    public RoutingService routingService(WebClient webClient, ObjectMapper objectMapper) {
        return new GraphHopperRoutingService(webClient, objectMapper);
    }
}

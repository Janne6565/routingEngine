package com.janne.routingsystem.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${service.backend_url}")
    private String BACKEND_URL;

    @Bean
    public WebClient webClient() {
        return WebClient.create(BACKEND_URL);
    }
}

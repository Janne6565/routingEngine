package com.janne.routingsystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ServiceChecker {

    private final RestTemplate restTemplate;

    public boolean isServiceUp(String url) {
        int maxRetries = 5;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                restTemplate.getForEntity(url, String.class);
                return true;
            } catch (Exception e) {
                retryCount++;
                try {
                    Thread.sleep(2000); // wait for 2 seconds before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return false;
    }
}

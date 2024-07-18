package com.janne.routingsystem.listeners;

import com.janne.routingsystem.service.ServiceChecker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {

    private final ServiceChecker serviceChecker;
    private static final Logger logger = LoggerFactory.getLogger(StartupListener.class);
    @Value("${service.backend_url}")
    private String graphhopperApiPath;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (serviceChecker.isServiceUp(graphhopperApiPath)) {
            logger.info("Graphhopper Service is up and reachable under: " + graphhopperApiPath);
        } else {
            logger.error("Graphhopper not reachable under: " + graphhopperApiPath + "\nMake sure you have graphhopper running locally and correctly configured the RootUrl in the spring application-properties");
            System.exit(1);
        }
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}

package com.janne.routingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
@EnableCaching
public class RoutingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoutingSystemApplication.class, args);
    }

}

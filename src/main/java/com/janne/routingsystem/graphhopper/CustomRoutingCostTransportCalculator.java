package com.janne.routingsystem.graphhopper;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.janne.routingsystem.model.CoordinateDto;
import com.janne.routingsystem.model.incoming.RouteResponse;
import com.janne.routingsystem.service.routingService.RoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomRoutingCostTransportCalculator extends AbstractForwardVehicleRoutingTransportCosts {

    private final RoutingService routingService;

    @Override
    @Cacheable(value = "distances", key = "#locationFrom.toString() + #locationTo.toString()")
    public double getDistance(Location locationFrom, Location locationTo, double v, Vehicle vehicle) {
        CoordinateDto from = CoordinateDto.fromLocation(locationFrom);
        CoordinateDto to = CoordinateDto.fromLocation(locationTo);
        RouteResponse routeResponse = routingService.calculateRoute(from, to);
        return routeResponse.getPaths().getFirst().getDistance();
    }

    @Override
    @Cacheable(value = "transportTimes", key = "#locationFrom.toString() + #locationTo.toString()")
    public double getTransportTime(Location locationFrom, Location locationTo, double v, Driver driver, Vehicle vehicle) {
        CoordinateDto from = CoordinateDto.fromLocation(locationFrom);
        CoordinateDto to = CoordinateDto.fromLocation(locationTo);
        RouteResponse routeResponse = routingService.calculateRoute(from, to);
        return (double) routeResponse.getPaths().getFirst().getTime() / 1000 / 60;
    }

    @Override
    @Cacheable(value = "transportCosts", key = "#locationFrom.toString() + #locationTo.toString()")
    public double getTransportCost(Location locationFrom, Location locationTo, double v, Driver driver, Vehicle vehicle) {
        CoordinateDto from = CoordinateDto.fromLocation(locationFrom);
        CoordinateDto to = CoordinateDto.fromLocation(locationTo);
        RouteResponse routeResponse = routingService.calculateRoute(from, to);
        return routeResponse.getPaths().getFirst().getTime();
    }
}

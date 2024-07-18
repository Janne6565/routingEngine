package com.janne.routingsystem.graphhopper;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.janne.routingsystem.model.Coordinate;
import com.janne.routingsystem.model.incoming.RouteResponse;
import com.janne.routingsystem.service.GraphHopperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomDistanceCalculator extends AbstractForwardVehicleRoutingTransportCosts {

    private final GraphHopperService graphHopperService;

    @Override
    public double getDistance(Location locationFrom, Location locationTo, double v, Vehicle vehicle) {
        Coordinate from = Coordinate.fromLocation(locationFrom);
        Coordinate to = Coordinate.fromLocation(locationTo);
        RouteResponse routeResponse = graphHopperService.calculateRoute(from, to);
        return routeResponse.getPaths().getFirst().getDistance();
    }

    @Override
    public double getTransportTime(Location locationFrom, Location locationTo, double v, Driver driver, Vehicle vehicle) {
        Coordinate from = Coordinate.fromLocation(locationFrom);
        Coordinate to = Coordinate.fromLocation(locationTo);
        RouteResponse routeResponse = graphHopperService.calculateRoute(from, to);
        return (double) routeResponse.getPaths().getFirst().getTime() / 1000 / 60;
    }

    @Override
    public double getTransportCost(Location locationFrom, Location locationTo, double v, Driver driver, Vehicle vehicle) {
        Coordinate from = Coordinate.fromLocation(locationFrom);
        Coordinate to = Coordinate.fromLocation(locationTo);
        RouteResponse routeResponse = graphHopperService.calculateRoute(from, to);
        return routeResponse.getPaths().getFirst().getWeight();
    }
}

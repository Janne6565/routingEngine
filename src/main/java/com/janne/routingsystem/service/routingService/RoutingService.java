package com.janne.routingsystem.service.routingService;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.janne.routingsystem.model.CoordinateDto;
import com.janne.routingsystem.model.incoming.RouteResponse;
import org.springframework.stereotype.Service;

public interface RoutingService {
    RouteResponse calculateRoute(CoordinateDto from, CoordinateDto to);
    VehicleRoutingTransportCostsMatrix buildDistanceMatrix(Location[] locations, boolean isSymmetric, String key);
}

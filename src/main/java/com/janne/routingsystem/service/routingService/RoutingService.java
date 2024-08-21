package com.janne.routingsystem.service.routingService;

import com.janne.routingsystem.model.CoordinateDto;
import com.janne.routingsystem.model.incoming.RouteResponse;
import org.springframework.stereotype.Service;

public interface RoutingService {
    public RouteResponse calculateRoute(CoordinateDto from, CoordinateDto to);
}

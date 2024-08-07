package com.janne.routingsystem.model;

import com.graphhopper.jsprit.core.problem.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Coordinate {
    private double lat;
    private double lng;

    public Coordinate(String value) {
        String[] params = value.split(",");
        lng = Double.parseDouble(params[0]);
        lat = Double.parseDouble(params[1]);
    }

    public static Coordinate fromLocation(Location location) {
        return Coordinate.builder()
                .lng(location.getCoordinate().getY())
                .lat(location.getCoordinate().getX())
                .build();
    }

    public String buildToJson() {
        return "[" + lat + "," + lng + "]";
    }

    public Location toLocation() {
        return Location.newInstance(lat, lng);
    }
}

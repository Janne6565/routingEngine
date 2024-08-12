package com.janne.routingsystem.model;

import com.graphhopper.jsprit.core.problem.Location;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoordinateDto {
    @NotNull
    private double lat;
    @NotNull
    private double lng;

    public CoordinateDto(String value) {
        String[] params = value.split(",");
        lat = Double.parseDouble(params[0]);
        lng = Double.parseDouble(params[1]);
    }

    public static CoordinateDto fromLocation(Location location) {
        return CoordinateDto.builder()
                .lng(location.getCoordinate().getY())
                .lat(location.getCoordinate().getX())
                .build();
    }

    public String buildToJson() {
        return "[" + lng + "," + lat + "]";
    }

    public Location toLocation() {
        return Location.newInstance(lat, lng);
    }
}

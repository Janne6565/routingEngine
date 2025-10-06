package com.janne.routingsystem.model;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.util.Coordinate;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoordinateDto {
    @NotNull
    private double lat;
    @NotNull
    private double lng;

    public CoordinateDto(String value) {
        String[] params = value.split(",");
        lng = Double.parseDouble(params[0]);
        lat = Double.parseDouble(params[1]);
    }

    public static CoordinateDto fromLocation(Location location) {
        return CoordinateDto.builder()
            .lng(location.getCoordinate().getX())
            .lat(location.getCoordinate().getY())
            .build();
    }

    public Location toLocation() {
        return Location.Builder.newInstance()
            .setCoordinate(new Coordinate(lng, lat))
            .setId("[" + lng + "," + lat + "]")
            .build();
    }

    public String buildToJson() {
        return "[" + lng + "," + lat + "]";
    }


}

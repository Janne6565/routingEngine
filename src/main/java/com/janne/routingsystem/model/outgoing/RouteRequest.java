package com.janne.routingsystem.model.outgoing;

import com.janne.routingsystem.model.Coordinate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {
    private Coordinate[] points;
    @Builder.Default
    private String profile = "car";

    public String buildJsonString() {
        return "{\"points\": [" + String.join(",", Arrays.stream(this.points).map(Coordinate::buildToJson).toList()) + "]," + "\"profile\": \"" + profile + "\"}";
    }
}

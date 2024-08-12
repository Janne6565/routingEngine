package com.janne.routingsystem.model.outgoing;

import com.janne.routingsystem.model.CoordinateDto;
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
    private CoordinateDto[] points;
    @Builder.Default
    private String profile = "car";

    public String buildJsonString() {
        return "{\"points\": [" + String.join(",", Arrays.stream(this.points).map(CoordinateDto::buildToJson).toList()) + "]," + "\"profile\": \"" + profile + "\"}";
    }
}

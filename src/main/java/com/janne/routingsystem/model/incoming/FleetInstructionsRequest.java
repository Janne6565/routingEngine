package com.janne.routingsystem.model.incoming;

import com.janne.routingsystem.model.Coordinate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FleetInstructionsRequest {

    private Coordinate[] startPositions;
    private Coordinate[] jobPositions;

}

package com.janne.routingsystem.model.incoming;

import com.janne.routingsystem.model.dto.JobDto;
import com.janne.routingsystem.model.dto.VehicleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FleetInstructionsRequest {

    private VehicleDto[] vehicles;
    private JobDto[] jobs;
    private String previousSolutionId;
}

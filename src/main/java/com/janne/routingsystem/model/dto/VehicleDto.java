package com.janne.routingsystem.model.dto;

import com.janne.routingsystem.model.CoordinateDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDto {
    private String id;
    @NotNull
    private CoordinateDto position;
    private double capacity;
    private double earliestTime;
    private double latestTime;
    private double serviceTime;
}

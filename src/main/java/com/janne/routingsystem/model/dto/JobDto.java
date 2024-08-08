package com.janne.routingsystem.model.dto;

import com.janne.routingsystem.model.Coordinate;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDto {
    private String id;
    @NotNull
    private Coordinate position;
    private double serviceTime;
    private double earliestTime;
    private double latestTime;
}

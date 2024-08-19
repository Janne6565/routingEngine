package com.janne.routingsystem.service.scheduling;

import com.janne.routingsystem.model.incoming.FleetInstructionsRequest;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScheduledTask {

    private final FleetInstructionsRequest request;
    private final String id;
}

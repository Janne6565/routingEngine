package com.janne.routingsystem.service.scheduling;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.janne.routingsystem.model.incoming.FleetInstructionsRequest;
import com.janne.routingsystem.service.VRPService;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SchedulingService {

    private final VRPService VRPService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ArrayList<ScheduledTask> tasks = new ArrayList<>();
    private final Map<String, VehicleRoutingProblemSolution> results = new HashMap<>();

    public boolean doesTaskExist(String uuid) {
        return results.containsKey(uuid);
    }

    public String scheduleTask(FleetInstructionsRequest request) {
        String uuid = UUID.randomUUID().toString();
        ScheduledTask task = ScheduledTask.builder()
                .id(uuid)
                .request(request)
                .build();
        tasks.add(task);
        results.put(uuid, null);

        logger.info("New Task scheduled: {}", task);
        return uuid;
    }

    public boolean isFinished(String uuid) {
        return results.getOrDefault(uuid, null) != null;
    }

    public @Nullable VehicleRoutingProblemSolution getResult(String uuid) {
        return results.getOrDefault(uuid, null);
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    private void runTasks() {
        if (tasks.size() == 0) {
            return;
        }

        ScheduledTask taskToRun = tasks.get(0);
        tasks.remove(0);
        FleetInstructionsRequest request = taskToRun.getRequest();
        logger.info("Starting job with uuid: {}", taskToRun.getId());
        VehicleRoutingProblemSolution solution = VRPService.calculateBestSolution(request.getVehicles(), request.getJobs(), Arrays.toString(request.getVehicles()) + " " + Arrays.toString(request.getJobs()));
        System.out.println(solution);
        results.put(taskToRun.getId(), solution);
        logger.info("Finished job with uuid: {}", taskToRun.getId());
    }
}

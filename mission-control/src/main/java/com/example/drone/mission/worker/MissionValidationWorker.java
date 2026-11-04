package com.example.drone.mission.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MissionValidationWorker {

    public void handle(JobClient client, ActivatedJob job) {
        try {
            String droneId = String.valueOf(job.getVariable("droneId"));
            Object waypoints = job.getVariable("waypoints");
            if (droneId.isBlank() || waypoints == null) {
                throw new IllegalArgumentException("droneId and waypoints are required");
            }

            client.newCompleteCommand(job.getKey())
                    .variables(Map.of("missionValidated", true))
                    .send()
                    .join();
        } catch (Exception exception) {
            log.error("Mission validation failed for job {}", job.getKey(), exception);
            client.newFailCommand(job.getKey())
                    .retries(0)
                    .errorMessage(exception.getMessage())
                    .send()
                    .join();
        }
    }
}

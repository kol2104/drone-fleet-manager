package com.example.drone.mission.worker;

import com.example.drone.common.workflow.JobRetryPolicy;
import com.example.drone.mission.event.MissionEventType;
import com.example.drone.mission.service.DroneEventLoggingService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogWorker {

    private final DroneEventLoggingService eventLoggingService;

    public void handle(JobClient client, ActivatedJob job) {
        try {
            String droneId = Objects.toString(job.getVariable("droneId"), null);
            String missionId = Objects.toString(job.getVariable("missionId"), "unknown");
            String flightStatus = Objects.toString(job.getVariable("flightStatus"), "UNKNOWN");
            String failureReason = Objects.toString(job.getVariable("flightFailureReason"), null);

            if (droneId == null || droneId.isBlank()) {
                throw new IllegalArgumentException("droneId is required");
            }

            boolean completed = "COMPLETED".equals(flightStatus);
            eventLoggingService.logEvent(
                    "mission-terminal-" + job.getProcessInstanceKey(),
                    droneId,
                    completed ? MissionEventType.MISSION_COMPLETED : MissionEventType.MISSION_FAILED,
                    "Mission " + missionId + " ended with status " + flightStatus
                            + (failureReason == null ? "" : ": " + failureReason));

            client.newCompleteCommand(job.getKey())
                    .send()
                    .join();

            log.info("Mission {} audit record stored for drone {}", missionId, droneId);
        } catch (Exception exception) {
            log.error("Could not store mission audit record for job {}", job.getKey(), exception);
            client.newFailCommand(job.getKey())
                    .retries(JobRetryPolicy.retriesAfterFailure(job.getRetries()))
                    .retryBackoff(JobRetryPolicy.retryBackoff())
                    .errorMessage(JobRetryPolicy.errorMessage(exception))
                    .send()
                    .join();
        }
    }
}

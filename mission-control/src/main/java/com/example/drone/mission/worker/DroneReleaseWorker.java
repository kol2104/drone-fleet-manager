package com.example.drone.mission.worker;

import com.example.drone.common.workflow.JobRetryPolicy;
import com.example.drone.common.enums.DroneFlightStatus;
import com.example.drone.mission.event.MissionEventType;
import com.example.drone.mission.client.DeviceRegistryClient;
import com.example.drone.mission.service.DroneEventLoggingService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DroneReleaseWorker {

    private final DroneEventLoggingService eventLoggingService;
    private final DeviceRegistryClient deviceRegistryClient;

    public void handle(JobClient client, ActivatedJob job) {
        try {
            String droneId = String.valueOf(job.getVariable("droneId"));
            String missionId = Long.toString(job.getProcessInstanceKey());
            String currentFlightStatus = String.valueOf(job.getVariable("flightStatus"));
            DroneFlightStatus flightStatus = currentFlightStatus.equals("PENDING")
                    ? DroneFlightStatus.TIMED_OUT
                    : DroneFlightStatus.valueOf(currentFlightStatus);
            Double batteryPercentage = job.getVariable("finalBatteryPercentage") instanceof Number battery
                    ? battery.doubleValue()
                    : null;
            String failureReason = Objects.toString(job.getVariable("flightFailureReason"), null);
            String reportedDroneId = Objects.toString(job.getVariable("reportedDroneId"), null);
            boolean resultIdentityMismatch = flightStatus != DroneFlightStatus.TIMED_OUT
                    && !droneId.equals(reportedDroneId);
            if (resultIdentityMismatch) {
                flightStatus = DroneFlightStatus.FAILED;
                batteryPercentage = null;
                failureReason = "Drone result identity does not match the reserved drone";
            }
            if (flightStatus == DroneFlightStatus.TIMED_OUT) {
                failureReason = "Drone did not report a flight result before the BPMN timeout";
            }
            MissionEventType flightEventType = flightStatus == DroneFlightStatus.COMPLETED
                    ? MissionEventType.FLIGHT_COMPLETED
                    : MissionEventType.FLIGHT_FAILED;
            String flightDescription = flightStatus == DroneFlightStatus.COMPLETED
                    ? "Drone reported flight completion with battery " + batteryPercentage + "%"
                    : Objects.toString(failureReason, "Drone reported flight failure");
            eventLoggingService.logEvent(
                    "flight-result-" + missionId,
                    droneId,
                    flightEventType,
                    flightDescription);
            var releasedDrone = deviceRegistryClient.releaseDrone(
                    UUID.fromString(droneId),
                    missionId,
                    flightStatus,
                    batteryPercentage);
            eventLoggingService.logEvent(
                    "drone-release-" + missionId,
                    droneId,
                    MissionEventType.DRONE_RELEASED,
                    "Drone reservation released; Registry status=" + releasedDrone.getStatus());
            Map<String, Object> variables = new HashMap<>();
            variables.put("droneReleased", true);
            variables.put("flightStatus", flightStatus.name());
            if (failureReason != null) {
                variables.put("flightFailureReason", failureReason);
            }
            if (resultIdentityMismatch) {
                variables.put("finalBatteryPercentage", null);
                variables.put("distanceTraveledMeters", null);
                variables.put("flightDurationSeconds", null);
            }
            client.newCompleteCommand(job.getKey()).variables(variables).send().join();
        } catch (Exception exception) {
            log.error("Drone release failed for job {}", job.getKey(), exception);
            client.newFailCommand(job.getKey())
                    .retries(JobRetryPolicy.retriesAfterFailure(job.getRetries()))
                    .retryBackoff(JobRetryPolicy.retryBackoff())
                    .errorMessage(JobRetryPolicy.errorMessage(exception))
                    .send()
                    .join();
        }
    }
}

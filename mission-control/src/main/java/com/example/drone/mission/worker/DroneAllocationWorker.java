package com.example.drone.mission.worker;

import com.example.drone.common.workflow.JobRetryPolicy;
import com.example.drone.common.dto.DroneDto;
import com.example.drone.mission.event.MissionEventType;
import com.example.drone.mission.client.DeviceRegistryClient;
import com.example.drone.mission.service.DroneEventLoggingService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DroneAllocationWorker {

    private final DroneEventLoggingService eventLoggingService;
    private final DeviceRegistryClient deviceRegistryClient;

    public void handleDroneAllocation(JobClient client, ActivatedJob job) {
        try {
            String requestedDroneId = job.getVariable("droneId").toString();
            log.info("Reserving requested drone: {}", requestedDroneId);

            DroneDto drone = deviceRegistryClient.reserveDrone(
                    UUID.fromString(requestedDroneId),
                    Long.toString(job.getProcessInstanceKey()));
            if (drone.getBatteryPercentage() == null) {
                throw new IllegalStateException("Device Registry returned no battery state for " + requestedDroneId);
            }

            Map<String, Object> variables = new HashMap<>();
            variables.put("allocatedDrone", drone.getDroneId());
            variables.put("batteryLevel", drone.getBatteryPercentage());
            variables.put("allocationTime", Instant.now().toString());
            variables.put("weatherVerified", true);
            variables.put("allocationSucceeded", true);

            eventLoggingService.logEvent(
                    "drone-allocation-" + job.getProcessInstanceKey(),
                    requestedDroneId,
                    MissionEventType.ALLOCATED,
                    "Drone reserved with reported battery: " + drone.getBatteryPercentage() + "%");
            client.newCompleteCommand(job.getKey())
                    .variables(variables)
                    .send()
                    .join();
            log.info("Drone allocated from Device Registry: {} with battery: {}%",
                    requestedDroneId, drone.getBatteryPercentage());
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                String droneId = Objects.toString(job.getVariable("droneId"), "unknown");
                try {
                    handleRejectedReservation(client, job, droneId, exception);
                } catch (Exception handlingException) {
                    failJob(client, job, handlingException);
                }
                return;
            }
            failJob(client, job, exception);
        } catch (Exception e) {
            log.error("Error allocating drone: {}", e.getMessage());
            failJob(client, job, e);
        }
    }

    private void handleRejectedReservation(
            JobClient client,
            ActivatedJob job,
            String droneId,
            RestClientResponseException exception) {
        String missionId = Long.toString(job.getProcessInstanceKey());
        String reason = exception.getStatusCode().equals(HttpStatus.CONFLICT)
                ? "Requested drone is not available"
                : "Device Registry rejected the reservation with HTTP " + exception.getStatusCode().value();

        eventLoggingService.logEvent(
                "allocation-rejected-" + missionId,
                droneId,
                MissionEventType.ALLOCATION_REJECTED,
                reason);
        client.newCompleteCommand(job.getKey())
                .variables(Map.of(
                        "allocationSucceeded", false,
                        "flightStatus", "FAILED",
                        "flightFailureReason", reason))
                .send()
                .join();
        log.warn("Reservation rejected for drone {} in mission {}: {}",
                droneId, missionId, exception.getStatusCode());
    }

    private void failJob(JobClient client, ActivatedJob job, Exception exception) {
        try {
            client.newFailCommand(job.getKey())
                    .retries(JobRetryPolicy.retriesAfterFailure(job.getRetries()))
                    .retryBackoff(JobRetryPolicy.retryBackoff())
                    .errorMessage(JobRetryPolicy.errorMessage(exception))
                    .send()
                    .join();
        } catch (Exception failException) {
            log.error("Failed to fail drone allocation job {}", job.getKey(), failException);
        }
    }
}

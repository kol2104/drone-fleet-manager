package com.example.drone.mission.worker;

import com.example.drone.common.workflow.JobRetryPolicy;
import com.example.drone.mission.mapper.DroneFlightCommandMapper;
import com.example.drone.mission.model.MissionProcessVariables;
import com.example.drone.mission.event.MissionEventType;
import com.example.drone.mission.service.DroneCommandPublisher;
import com.example.drone.mission.service.DroneEventLoggingService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlightExecutionWorker {

    private final DroneEventLoggingService eventLoggingService;
    private final DroneCommandPublisher commandPublisher;
    private final DroneFlightCommandMapper commandMapper;

    public void handleFlightExecution(JobClient client, ActivatedJob job) {
        try {
            MissionProcessVariables mission = job.getVariablesAsType(MissionProcessVariables.class);
            String missionId = Long.toString(job.getProcessInstanceKey());
            Double batteryLevel = ((Number) job.getVariable("batteryLevel")).doubleValue();
            var command = commandMapper.toEvent(mission, missionId, batteryLevel, Instant.now());
            commandPublisher.publish(command);

            eventLoggingService.logEvent(
                    "flight-command-" + missionId,
                    command.getDroneId(),
                    MissionEventType.FLIGHT_COMMAND_SENT,
                    "Flight command published for mission " + missionId);
            client.newCompleteCommand(job.getKey())
                    .variables(Map.of(
                            "flightCorrelationId", missionId,
                            "flightStatus", "PENDING"))
                    .send()
                    .join();
            log.info("Flight command published for drone {} and mission {}",
                    command.getDroneId(), missionId);
        } catch (Exception e) {
            log.error("Error executing flight: {}", e.getMessage());
            try {
                client.newFailCommand(job.getKey())
                        .retries(JobRetryPolicy.retriesAfterFailure(job.getRetries()))
                        .retryBackoff(JobRetryPolicy.retryBackoff())
                        .errorMessage(JobRetryPolicy.errorMessage(e))
                        .send()
                        .join();
            } catch (Exception ex) {
                log.error("Failed to fail job: {}", ex.getMessage());
            }
        }
    }
}

package com.example.drone.mission.worker;

import com.example.drone.common.workflow.JobRetryPolicy;
import com.example.drone.mission.event.MissionEventType;
import com.example.drone.mission.model.MissionProcessVariables;
import com.example.drone.mission.service.DroneEventLoggingService;
import com.example.drone.mission.weather.WeatherCheckClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherCheckWorker {

    private final DroneEventLoggingService eventLoggingService;
    private final WeatherCheckClient weatherCheckClient;

    public void handleWeatherCheck(JobClient client, ActivatedJob job) {
        try {
            MissionProcessVariables mission = job.getVariablesAsType(MissionProcessVariables.class);
            String droneId = mission.droneId();
            String missionId = Long.toString(job.getProcessInstanceKey());
            log.info("Checking weather for drone: {}", droneId);

            boolean weatherOk = weatherCheckClient.isSafeToFly(mission.waypoints());
            String checkedAt = Instant.now().toString();
            Map<String, Object> variables = new HashMap<>();
            variables.put("weatherOk", weatherOk);
            variables.put("weatherVerified", true);
            variables.put("weatherCheckTime", checkedAt);

            if (weatherOk) {
                eventLoggingService.logEvent(
                    "weather-checked-" + missionId,
                    droneId,
                    MissionEventType.WEATHER_CHECK_OK,
                    "Weather check completed: conditions are good"
                );

            } else {
                variables.put("flightStatus", "FAILED");
                variables.put("flightFailureReason", "Bad weather conditions");
                eventLoggingService.logEvent(
                    "weather-failed-" + missionId,
                    droneId,
                    MissionEventType.WEATHER_CHECK_FAILED,
                    "Weather check failed: bad conditions"
                );
            }

            client.newCompleteCommand(job.getKey())
                    .variables(variables)
                    .send()
                    .join();
            log.info("Weather check completed for drone {}: {}", droneId, weatherOk);
        } catch (Exception e) {
            log.error("Error in weather check: {}", e.getMessage());
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

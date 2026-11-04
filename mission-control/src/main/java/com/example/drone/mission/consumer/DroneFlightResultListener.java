package com.example.drone.mission.consumer;

import com.example.drone.common.enums.DroneFlightStatus;
import com.example.drone.common.event.DroneFlightResultEvent;
import com.example.drone.common.messaging.DroneFlightMessaging;
import com.example.drone.mission.mapper.DroneFlightResultMapper;
import io.camunda.zeebe.client.ZeebeClient;
import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DroneFlightResultListener {

    private final ZeebeClient zeebeClient;
    private final DroneFlightResultMapper resultMapper;

    @RabbitListener(queues = DroneFlightMessaging.RESULT_QUEUE)
    public void handle(DroneFlightResultEvent result) {
        validate(result);
        zeebeClient.newPublishMessageCommand()
                .messageName(DroneFlightMessaging.RESULT_MESSAGE_NAME)
                .correlationKey(result.getMissionId())
                .timeToLive(Duration.ofMinutes(15))
                .variables(resultMapper.toProcessVariables(result))
                .send()
                .join();
        log.info("Published drone result for mission {} from drone {}: {}",
                result.getMissionId(), result.getDroneId(), result.getStatus());
    }

    private void validate(DroneFlightResultEvent result) {
        if (result == null
                || result.getMissionId() == null
                || result.getMissionId().isBlank()
                || result.getDroneId() == null
                || result.getDroneId().isBlank()
                || result.getStatus() == null) {
            throw new IllegalArgumentException("Drone flight result is missing required fields");
        }
        if (result.getStatus() == DroneFlightStatus.COMPLETED
                && (result.getFinalBatteryPercentage() == null
                || !Double.isFinite(result.getFinalBatteryPercentage())
                || result.getFinalBatteryPercentage() < 0
                || result.getFinalBatteryPercentage() > 100)) {
            throw new IllegalArgumentException("Completed flight result must report a valid battery percentage");
        }
        if (result.getStatus() == DroneFlightStatus.FAILED
                && (result.getFailureReason() == null || result.getFailureReason().isBlank())) {
            throw new IllegalArgumentException("Failed flight result must include a failure reason");
        }
    }
}

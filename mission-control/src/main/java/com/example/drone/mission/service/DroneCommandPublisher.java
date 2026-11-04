package com.example.drone.mission.service;

import com.example.drone.common.event.DroneFlightCommandEvent;
import com.example.drone.common.messaging.DroneFlightMessaging;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DroneCommandPublisher {

    private static final long PUBLISH_CONFIRM_TIMEOUT_SECONDS = 5;

    private final RabbitTemplate rabbitTemplate;

    public void publish(DroneFlightCommandEvent command) throws Exception {
        CorrelationData correlationData = new CorrelationData(command.getMissionId());
        rabbitTemplate.convertAndSend(
                DroneFlightMessaging.EXCHANGE_NAME,
                DroneFlightMessaging.COMMAND_ROUTING_KEY_PREFIX + command.getDroneId(),
                command,
                correlationData);

        CorrelationData.Confirm confirm = correlationData.getFuture()
                .get(PUBLISH_CONFIRM_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!confirm.isAck()) {
            throw new IllegalStateException("RabbitMQ did not confirm flight command: " + confirm.getReason());
        }
        if (correlationData.getReturned() != null) {
            throw new IllegalStateException("No drone gateway is bound for drone " + command.getDroneId());
        }
    }
}

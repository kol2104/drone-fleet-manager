package com.example.drone.registry.event;

import com.example.drone.registry.mapper.DroneRegistryEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DroneRegistryEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final DroneRegistryEventMapper eventMapper;

    public void publishDroneRegistered(String droneId, String deviceName, String userId) {
        publish(eventMapper.toRegisteredAudit(
                UUID.randomUUID().toString(),
                droneId,
                deviceName,
                userId,
                LocalDateTime.now()
        ));
    }

    public void publishDroneStatusChanged(
            String droneId,
            String previousStatus,
            String newStatus,
            String userId,
            long durationMs) {
        publish(eventMapper.toStatusChangedAudit(
                UUID.randomUUID().toString(),
                droneId,
                previousStatus,
                newStatus,
                userId,
                LocalDateTime.now(),
                durationMs
        ));
    }

    public void publishHeartbeat(String droneId, long durationMs) {
        publish(eventMapper.toHeartbeatAudit(
                UUID.randomUUID().toString(),
                droneId,
                LocalDateTime.now(),
                durationMs
        ));
    }

    private void publish(DroneRegistryAuditEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}

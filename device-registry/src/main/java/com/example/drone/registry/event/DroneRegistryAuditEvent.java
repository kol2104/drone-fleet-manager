package com.example.drone.registry.event;

import java.time.LocalDateTime;

public record DroneRegistryAuditEvent(
        String eventId,
        String droneId,
        String eventType,
        String status,
        String details,
        String userId,
        LocalDateTime timestamp,
        Long durationMs
) {
}

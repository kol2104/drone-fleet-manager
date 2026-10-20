package com.example.drone.registry.mapper;

import com.example.drone.registry.event.DroneRegistryAuditEvent;
import com.example.drone.registry.event.DroneRegistryEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DroneRegistryEventMapper {

    @Mapping(target = "source", constant = "DEVICE_REGISTRY")
    DroneRegistryEvent toDocument(DroneRegistryAuditEvent auditEvent);

    @Mapping(target = "eventType", constant = "REGISTERED")
    @Mapping(target = "status", constant = "SUCCESS")
    @Mapping(target = "details", expression = "java(\"Drone registered: \" + deviceName)")
    @Mapping(target = "durationMs", ignore = true)
    DroneRegistryAuditEvent toRegisteredAudit(
            String eventId,
            String droneId,
            String deviceName,
            String userId,
            LocalDateTime timestamp
    );

    @Mapping(target = "eventType", constant = "STATUS_CHANGED")
    @Mapping(target = "status", source = "newStatus")
    @Mapping(target = "details", expression = "java(\"Status changed: \" + previousStatus + \" -> \" + newStatus)")
    DroneRegistryAuditEvent toStatusChangedAudit(
            String eventId,
            String droneId,
            String previousStatus,
            String newStatus,
            String userId,
            LocalDateTime timestamp,
            Long durationMs
    );

    @Mapping(target = "eventType", constant = "HEARTBEAT")
    @Mapping(target = "status", constant = "RECEIVED")
    @Mapping(target = "details", constant = "Heartbeat received")
    @Mapping(target = "userId", ignore = true)
    DroneRegistryAuditEvent toHeartbeatAudit(
            String eventId,
            String droneId,
            LocalDateTime timestamp,
            Long durationMs
    );
}

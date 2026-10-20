package com.example.drone.mission.mapper;

import com.example.drone.mission.event.MissionEvent;
import com.example.drone.mission.event.MissionEventType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MissionEventMapper {

    @Mapping(target = "eventId", expression = "java(generateEventId())")
    @Mapping(target = "eventType", expression = "java(eventType.name())")
    @Mapping(target = "severity", expression = "java(eventType.getSeverity())")
    @Mapping(target = "source", constant = "MISSION_CONTROL")
    @Mapping(target = "timestamp", expression = "java(currentTimestamp())")
    MissionEvent toEvent(String droneId, MissionEventType eventType, String description);

    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "eventType", expression = "java(eventType.name())")
    @Mapping(target = "severity", expression = "java(eventType.getSeverity())")
    @Mapping(target = "source", constant = "MISSION_CONTROL")
    @Mapping(target = "timestamp", expression = "java(currentTimestamp())")
    MissionEvent toEvent(String eventId, String droneId, MissionEventType eventType, String description);

    default String generateEventId() {
        return UUID.randomUUID().toString();
    }

    default Instant currentTimestamp() {
        return Instant.now();
    }
}

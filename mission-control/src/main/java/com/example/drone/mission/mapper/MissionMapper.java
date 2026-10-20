package com.example.drone.mission.mapper;

import com.example.drone.common.dto.MissionDto;
import com.example.drone.mission.model.MissionProcessVariables;
import com.example.drone.mission.model.MissionStatusResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MissionMapper {

    @Mapping(target = "missionId", expression = "java(generateMissionId())")
    MissionProcessVariables toProcessVariables(MissionDto missionRequest);

    MissionStatusResponse toStatusResponse(long processInstanceKey, String status);

    @Mapping(target = "missionId", source = "processInstanceKey")
    @Mapping(target = "status", constant = "EXECUTING")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    MissionDto toStartedResponse(
            MissionDto request,
            String processInstanceKey,
            LocalDateTime createdAt
    );

    default String generateMissionId() {
        return UUID.randomUUID().toString();
    }
}

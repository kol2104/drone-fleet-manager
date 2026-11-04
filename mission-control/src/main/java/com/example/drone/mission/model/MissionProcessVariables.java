package com.example.drone.mission.model;

import com.example.drone.common.dto.WaypointDto;

import java.util.List;

public record MissionProcessVariables(
        String droneId,
        List<WaypointDto> waypoints,
        String initiatedBy,
        String missionId
) {
}

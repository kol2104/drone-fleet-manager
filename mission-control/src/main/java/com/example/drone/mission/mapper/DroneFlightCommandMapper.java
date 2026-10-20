package com.example.drone.mission.mapper;

import com.example.drone.common.event.DroneFlightCommandEvent;
import com.example.drone.mission.model.MissionProcessVariables;
import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DroneFlightCommandMapper {

    @Mapping(target = "missionId", source = "missionId")
    @Mapping(target = "droneId", source = "mission.droneId")
    @Mapping(target = "waypoints", source = "mission.waypoints")
    @Mapping(target = "startingBatteryPercentage", source = "startingBatteryPercentage")
    @Mapping(target = "requestedAt", source = "requestedAt")
    DroneFlightCommandEvent toEvent(
            MissionProcessVariables mission,
            String missionId,
            Double startingBatteryPercentage,
            Instant requestedAt);
}

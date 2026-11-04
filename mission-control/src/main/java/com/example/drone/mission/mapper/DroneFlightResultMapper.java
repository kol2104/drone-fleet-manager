package com.example.drone.mission.mapper;

import com.example.drone.common.event.DroneFlightResultEvent;
import com.example.drone.mission.model.DroneFlightResultVariables;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DroneFlightResultMapper {

    @Mapping(target = "reportedDroneId", source = "droneId")
    @Mapping(target = "flightStatus", source = "status")
    @Mapping(target = "finalBatteryPercentage", source = "finalBatteryPercentage")
    @Mapping(target = "distanceTraveledMeters", source = "distanceTraveledMeters")
    @Mapping(target = "flightDurationSeconds", source = "flightDurationSeconds")
    @Mapping(target = "flightFailureReason", source = "failureReason")
    DroneFlightResultVariables toProcessVariables(DroneFlightResultEvent event);
}

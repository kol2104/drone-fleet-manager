package com.example.drone.registry.mapper;

import com.example.drone.common.dto.DroneDto;
import com.example.drone.common.enums.DroneStatusCode;
import com.example.drone.registry.domain.DroneStatus;
import com.example.drone.registry.entity.Drone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DroneMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "reservationId", ignore = true)
    @Mapping(target = "lastHeartbeat", ignore = true)
    @Mapping(target = "registeredAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Drone toEntity(DroneDto droneDto);

    @Mapping(target = "droneId", source = "id")
    DroneDto toDto(Drone drone);

    DroneStatus toDomain(DroneStatusCode statusCode);

    DroneStatusCode toContract(DroneStatus status);

    String toStatusName(DroneStatus status);
}

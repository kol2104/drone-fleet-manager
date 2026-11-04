package com.example.drone.mission.client;

import com.example.drone.common.dto.DroneDto;
import com.example.drone.common.dto.DroneReservationReleaseRequest;
import com.example.drone.common.dto.DroneReservationRequest;
import com.example.drone.common.enums.DroneFlightStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class DeviceRegistryClient {

    private final RestClient deviceRegistryRestClient;

    public DroneDto reserveDrone(UUID droneId, String missionId) {
        return deviceRegistryRestClient.post()
                .uri("/api/v1/devices/{droneId}/reservations", droneId)
                .body(new DroneReservationRequest(missionId))
                .retrieve()
                .body(DroneDto.class);
    }

    public DroneDto releaseDrone(
            UUID droneId,
            String missionId,
            DroneFlightStatus flightStatus,
            Double batteryPercentage) {
        return deviceRegistryRestClient.post()
                .uri("/api/v1/devices/{droneId}/reservations/{missionId}/release", droneId, missionId)
                .body(new DroneReservationReleaseRequest(flightStatus, batteryPercentage))
                .retrieve()
                .body(DroneDto.class);
    }
}

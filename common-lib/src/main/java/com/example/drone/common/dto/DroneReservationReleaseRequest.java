package com.example.drone.common.dto;

import com.example.drone.common.enums.DroneFlightStatus;

public record DroneReservationReleaseRequest(
        DroneFlightStatus flightStatus,
        Double batteryPercentage
) {
}

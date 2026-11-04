package com.example.drone.mission.model;

public record DroneFlightResultVariables(
        String reportedDroneId,
        String flightStatus,
        Double finalBatteryPercentage,
        Long distanceTraveledMeters,
        Long flightDurationSeconds,
        String flightFailureReason
) {
}

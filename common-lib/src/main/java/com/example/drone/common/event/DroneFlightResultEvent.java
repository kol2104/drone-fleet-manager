package com.example.drone.common.event;

import com.example.drone.common.enums.DroneFlightStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DroneFlightResultEvent {
    private String missionId;
    private String droneId;
    private DroneFlightStatus status;
    private Double finalBatteryPercentage;
    private Long distanceTraveledMeters;
    private Long flightDurationSeconds;
    private String failureReason;
    private Instant completedAt;
}

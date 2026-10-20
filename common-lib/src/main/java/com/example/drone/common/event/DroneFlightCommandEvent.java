package com.example.drone.common.event;

import com.example.drone.common.dto.WaypointDto;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DroneFlightCommandEvent {
    private String missionId;
    private String droneId;
    private List<WaypointDto> waypoints;
    private Double startingBatteryPercentage;
    private Instant requestedAt;
}

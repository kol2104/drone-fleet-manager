package com.example.drone.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionDto {
    private String missionId;
    private String droneId;
    private String status;
    private List<WaypointDto> waypoints;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String initiatedBy;
    private String failureReason;
}

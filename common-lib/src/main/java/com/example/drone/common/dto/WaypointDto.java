package com.example.drone.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaypointDto {
    private Integer sequence;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Integer dwellTimeSeconds;
}

package com.example.drone.common.dto;

import com.example.drone.common.enums.DroneStatusCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DroneDto {
    private UUID droneId;
    @NotBlank
    private String identifier;
    @NotBlank
    private String deviceName;
    @NotBlank
    private String model;
    private String manufacturer;
    private Integer maxFlightTimeMinutes;
    private Float maxAltitudeMeters;
    private Float maxSpeedMs;
    private Float maxPayloadKg;
    private DroneStatusCode status;
    @NotNull
    private Double latitude;
    @NotNull
    private Double longitude;
    @NotNull
    private Double batteryPercentage;
    @NotBlank
    private String ownerUserId;
    private Map<String, Object> metadata;
    private LocalDateTime lastHeartbeat;
    private LocalDateTime registeredAt;
}

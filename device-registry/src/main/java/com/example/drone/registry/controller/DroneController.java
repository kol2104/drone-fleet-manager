package com.example.drone.registry.controller;

import com.example.drone.common.dto.DroneDto;
import com.example.drone.common.dto.DroneReservationReleaseRequest;
import com.example.drone.common.dto.DroneReservationRequest;
import com.example.drone.common.enums.DroneStatusCode;
import com.example.drone.registry.entity.Drone;
import com.example.drone.registry.mapper.DroneMapper;
import com.example.drone.registry.service.DroneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DroneController {

    private final DroneService droneService;
    private final DroneMapper droneMapper;

    @PostMapping("/register")
    public ResponseEntity<DroneDto> registerDrone(@Valid @RequestBody DroneDto droneDto) {
        Drone drone = droneService.registerDrone(droneDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(droneMapper.toDto(drone));
    }

    @GetMapping("/{droneId}")
    public ResponseEntity<DroneDto> getDrone(@PathVariable UUID droneId) {
        Drone drone = droneService.getDroneById(droneId);
        return ResponseEntity.ok(droneMapper.toDto(drone));
    }

    @GetMapping("/owner/{userId}")
    public ResponseEntity<List<DroneDto>> getDronesByOwner(@PathVariable String userId) {
        List<Drone> drones = droneService.getDronesByOwner(userId);
        return ResponseEntity.ok(drones.stream()
                .map(droneMapper::toDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DroneDto>> getDronesByStatus(@PathVariable DroneStatusCode status) {
        List<Drone> drones = droneService.getDronesByStatus(droneMapper.toDomain(status));
        return ResponseEntity.ok(drones.stream()
                .map(droneMapper::toDto)
                .collect(Collectors.toList()));
    }

    @PutMapping("/{droneId}/status")
    public ResponseEntity<DroneDto> updateStatus(@PathVariable UUID droneId, @RequestParam DroneStatusCode status) {
        Drone drone = droneService.updateDroneStatus(droneId, droneMapper.toDomain(status));
        return ResponseEntity.ok(droneMapper.toDto(drone));
    }

    @PostMapping("/{droneId}/heartbeat")
    public ResponseEntity<Void> heartbeat(@PathVariable UUID droneId) {
        droneService.updateHeartbeat(droneId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available/{userId}")
    public ResponseEntity<List<DroneDto>> getAvailable(@PathVariable String userId) {
        List<Drone> drones = droneService.getAvailableDrones(userId);
        return ResponseEntity.ok(drones.stream()
                .map(droneMapper::toDto)
                .collect(Collectors.toList()));
    }

    @PostMapping("/{droneId}/reservations")
    public ResponseEntity<DroneDto> reserveDrone(
            @PathVariable UUID droneId,
            @RequestBody DroneReservationRequest request) {
        Drone drone = droneService.reserveDrone(droneId, request.missionId());
        return ResponseEntity.ok(droneMapper.toDto(drone));
    }

    @PostMapping("/{droneId}/reservations/{missionId}/release")
    public ResponseEntity<DroneDto> releaseDrone(
            @PathVariable UUID droneId,
            @PathVariable String missionId,
            @RequestBody DroneReservationReleaseRequest request) {
        Drone drone = droneService.releaseDrone(
                droneId,
                missionId,
                request.flightStatus(),
                request.batteryPercentage());
        return ResponseEntity.ok(droneMapper.toDto(drone));
    }
}

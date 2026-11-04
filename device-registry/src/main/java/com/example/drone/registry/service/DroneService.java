package com.example.drone.registry.service;

import com.example.drone.common.dto.DroneDto;
import com.example.drone.common.enums.DroneFlightStatus;
import com.example.drone.registry.domain.DroneStatus;
import com.example.drone.registry.entity.Drone;
import com.example.drone.registry.mapper.DroneMapper;
import com.example.drone.registry.repository.DroneRepository;
import com.example.drone.registry.event.DroneRegistryEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class DroneService {

    private final DroneRepository droneRepository;
    private final DroneRegistryEventPublisher eventPublisher;
    private final DroneMapper droneMapper;
    private final double minimumBatteryPercentage;

    public DroneService(
            DroneRepository droneRepository,
            DroneRegistryEventPublisher eventPublisher,
            DroneMapper droneMapper,
            @Value("${device-registry.minimum-battery-percentage}") double minimumBatteryPercentage) {
        this.droneRepository = droneRepository;
        this.eventPublisher = eventPublisher;
        this.droneMapper = droneMapper;
        this.minimumBatteryPercentage = minimumBatteryPercentage;
    }

    public Drone registerDrone(DroneDto droneDto) {
        Drone drone = droneMapper.toEntity(droneDto);
        drone.setStatus(DroneStatus.IDLE);
        drone.setLastHeartbeat(LocalDateTime.now());
        Drone saved = droneRepository.save(drone);

        eventPublisher.publishDroneRegistered(saved.getId().toString(), saved.getDeviceName(), droneDto.getOwnerUserId());

        log.info("Drone registered: {}", saved.getId());
        return saved;
    }

    public Drone updateDroneStatus(UUID droneId, DroneStatus newStatus) {
        long startTime = System.currentTimeMillis();
        Drone drone = findDroneForUpdate(droneId);

        DroneStatus previousStatus = drone.getStatus();
        if ((newStatus == DroneStatus.IN_FLIGHT && previousStatus != DroneStatus.IN_FLIGHT)
                || (previousStatus == DroneStatus.IN_FLIGHT && newStatus != DroneStatus.IN_FLIGHT)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Use the mission reservation endpoints to change IN_FLIGHT status");
        }
        drone.setStatus(newStatus);
        drone.setLastHeartbeat(LocalDateTime.now());

        Drone updated = droneRepository.save(drone);
        long duration = System.currentTimeMillis() - startTime;

        eventPublisher.publishDroneStatusChanged(
                droneId.toString(),
                droneMapper.toStatusName(previousStatus),
                droneMapper.toStatusName(newStatus),
                "SYSTEM",
                duration
        );

        log.info("Drone {} status changed: {} -> {}", droneId, previousStatus, newStatus);
        return updated;
    }

    public Drone updateHeartbeat(UUID droneId) {
        long startTime = System.currentTimeMillis();
        Drone drone = findDroneForUpdate(droneId);

        drone.setLastHeartbeat(LocalDateTime.now());
        Drone updated = droneRepository.save(drone);
        long duration = System.currentTimeMillis() - startTime;

        eventPublisher.publishHeartbeat(droneId.toString(), duration);

        return updated;
    }

    public Drone reserveDrone(UUID droneId, String missionId) {
        if (missionId == null || missionId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "missionId is required");
        }

        long startTime = System.currentTimeMillis();
        Drone drone = findDroneForUpdate(droneId);
        if (Objects.equals(drone.getReservationId(), missionId)) {
            if (drone.getStatus() == DroneStatus.IN_FLIGHT) {
                return drone;
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This mission already released the drone");
        }
        if (drone.getStatus() != DroneStatus.IDLE
                || drone.getBatteryPercentage() < minimumBatteryPercentage) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Drone is not available for this mission");
        }

        DroneStatus previousStatus = drone.getStatus();
        drone.setStatus(DroneStatus.IN_FLIGHT);
        drone.setReservationId(missionId);
        Drone reserved = droneRepository.save(drone);
        eventPublisher.publishDroneStatusChanged(
                droneId.toString(),
                droneMapper.toStatusName(previousStatus),
                droneMapper.toStatusName(reserved.getStatus()),
                "MISSION_CONTROL",
                System.currentTimeMillis() - startTime
        );
        log.info("Drone {} reserved for mission {}", droneId, missionId);
        return reserved;
    }

    public Drone releaseDrone(
            UUID droneId,
            String missionId,
            DroneFlightStatus flightStatus,
            Double batteryPercentage) {
        if (missionId == null || missionId.isBlank() || flightStatus == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "missionId and flightStatus are required");
        }
        if (batteryPercentage != null
                && (!Double.isFinite(batteryPercentage) || batteryPercentage < 0 || batteryPercentage > 100)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "batteryPercentage must be between 0 and 100");
        }

        long startTime = System.currentTimeMillis();
        Drone drone = findDroneForUpdate(droneId);
        if (Objects.equals(drone.getReservationId(), missionId) && drone.getStatus() != DroneStatus.IN_FLIGHT) {
            return drone;
        }
        if (!Objects.equals(drone.getReservationId(), missionId) || drone.getStatus() != DroneStatus.IN_FLIGHT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mission does not own this drone reservation");
        }

        DroneStatus previousStatus = drone.getStatus();
        if (batteryPercentage != null) {
            drone.setBatteryPercentage(batteryPercentage);
        }
        drone.setStatus(statusAfterFlight(flightStatus, batteryPercentage));
        Drone released = droneRepository.save(drone);
        eventPublisher.publishDroneStatusChanged(
                droneId.toString(),
                droneMapper.toStatusName(previousStatus),
                droneMapper.toStatusName(released.getStatus()),
                "MISSION_CONTROL",
                System.currentTimeMillis() - startTime
        );
        log.info("Drone {} released for mission {} with status {} and battery {}",
                droneId, missionId, released.getStatus(), released.getBatteryPercentage());
        return released;
    }

    public Drone getDroneById(UUID droneId) {
        return droneRepository.findById(droneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Drone not found: " + droneId));
    }

    public List<Drone> getDronesByOwner(String userId) {
        return droneRepository.findByOwnerUserId(userId);
    }

    public List<Drone> getDronesByStatus(DroneStatus status) {
        return droneRepository.findByStatus(status);
    }

    public List<Drone> getAvailableDrones(String userId) {
        return droneRepository.findByStatusAndOwner(DroneStatus.IDLE, userId, minimumBatteryPercentage);
    }

    private Drone findDroneForUpdate(UUID droneId) {
        return droneRepository.findByIdForUpdate(droneId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Drone not found: " + droneId));
    }

    private DroneStatus statusAfterFlight(DroneFlightStatus flightStatus, Double batteryPercentage) {
        if (flightStatus == DroneFlightStatus.TIMED_OUT) {
            return DroneStatus.OFFLINE;
        }
        if (flightStatus == DroneFlightStatus.FAILED
                || (batteryPercentage != null && batteryPercentage < minimumBatteryPercentage)) {
            return DroneStatus.GROUNDED;
        }
        return DroneStatus.IDLE;
    }

}

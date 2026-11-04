package com.example.drone.registry.repository;

import com.example.drone.registry.domain.DroneStatus;
import com.example.drone.registry.entity.Drone;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DroneRepository extends JpaRepository<Drone, UUID> {
    List<Drone> findByOwnerUserId(String ownerUserId);

    List<Drone> findByStatus(DroneStatus status);

    @Query("SELECT d FROM Drone d WHERE d.status = :status AND d.ownerUserId = :userId "
            + "AND d.batteryPercentage >= :minimumBatteryPercentage")
    List<Drone> findByStatusAndOwner(
            @Param("status") DroneStatus status,
            @Param("userId") String userId,
            @Param("minimumBatteryPercentage") double minimumBatteryPercentage);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Drone d WHERE d.id = :droneId")
    Optional<Drone> findByIdForUpdate(@Param("droneId") UUID droneId);
}

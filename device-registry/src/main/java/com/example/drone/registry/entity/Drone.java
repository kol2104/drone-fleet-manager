package com.example.drone.registry.entity;

import com.example.drone.registry.domain.DroneStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "drones", indexes = {
    @Index(name = "idx_drone_status", columnList = "status"),
    @Index(name = "idx_drone_owner", columnList = "owner_user_id"),
    @Index(name = "idx_drone_heartbeat", columnList = "last_heartbeat")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Drone {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String identifier;

    @Column(nullable = false, unique = true)
    private String deviceName;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(length = 100)
    private String manufacturer;

    @Column
    private Integer maxFlightTimeMinutes;

    @Column
    private Float maxAltitudeMeters;

    @Column
    private Float maxSpeedMs;

    @Column
    private Float maxPayloadKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DroneStatus status;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double batteryPercentage;

    @Column(name = "reservation_id")
    private String reservationId;

    @Column(nullable = false)
    private String ownerUserId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(nullable = false)
    private LocalDateTime lastHeartbeat;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime registeredAt;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

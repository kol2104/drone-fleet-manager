package com.example.drone.mission.service;

import com.example.drone.common.dto.MissionDto;
import com.example.drone.mission.event.MissionEvent;
import com.example.drone.mission.event.MissionEventRepository;
import com.example.drone.mission.event.MissionEventType;
import com.example.drone.mission.mapper.MissionMapper;
import com.example.drone.mission.model.MissionStatusResponse;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MissionService {

    private static final String DRONE_MISSION_PROCESS_ID = "drone-flight-mission";

    private final ZeebeClient zeebeClient;
    private final DroneEventLoggingService eventLoggingService;
    private final MissionMapper missionMapper;
    private final MissionEventRepository missionEventRepository;

    public MissionDto startMission(MissionDto missionRequest) {
        validateMissionRequest(missionRequest);
        log.info("Starting mission for drone: {}", missionRequest.getDroneId());

        ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(DRONE_MISSION_PROCESS_ID)
                .latestVersion()
                .variables(missionMapper.toProcessVariables(missionRequest))
                .send()
                .join();

        long processInstanceKey = event.getProcessInstanceKey();
        log.info("Mission started with processInstanceKey: {}", processInstanceKey);

        eventLoggingService.logEvent(
                "mission-started-" + processInstanceKey,
                missionRequest.getDroneId(),
                MissionEventType.MISSION_STARTED,
                "Mission started for drone: " + missionRequest.getDroneId() +
                " (processInstanceKey: " + processInstanceKey + ")");

        return missionMapper.toStartedResponse(
                missionRequest,
                Long.toString(processInstanceKey),
                LocalDateTime.now());
    }

    public MissionStatusResponse getMissionStatus(long processInstanceKey) {
        log.info("Fetching status for processInstanceKey: {}", processInstanceKey);
        String processKey = Long.toString(processInstanceKey);
        if (missionEventRepository.findById("mission-started-" + processKey).isEmpty()) {
            throw new MissionNotFoundException(processInstanceKey);
        }

        String status = missionEventRepository.findById("mission-terminal-" + processKey)
                .map(MissionEvent::getEventType)
                .map(this::toMissionStatus)
                .orElse("EXECUTING");
        return missionMapper.toStatusResponse(processInstanceKey, status);
    }

    private String toMissionStatus(String eventType) {
        if (MissionEventType.MISSION_COMPLETED.name().equals(eventType)) {
            return "COMPLETED";
        }
        if (MissionEventType.MISSION_FAILED.name().equals(eventType)) {
            return "FAILED";
        }
        throw new IllegalStateException("Unexpected terminal mission event: " + eventType);
    }

    public static class MissionNotFoundException extends RuntimeException {
        public MissionNotFoundException(long processInstanceKey) {
            super("Mission not found: " + processInstanceKey);
        }
    }

    private void validateMissionRequest(MissionDto missionRequest) {
        if (missionRequest == null) {
            throw new IllegalArgumentException("Mission request must not be null");
        }
        if (missionRequest.getDroneId() == null || missionRequest.getDroneId().isBlank()) {
            throw new IllegalArgumentException("Mission request must contain droneId");
        }
        if (missionRequest.getWaypoints() == null || missionRequest.getWaypoints().isEmpty()) {
            throw new IllegalArgumentException("Mission request must contain at least one waypoint");
        }
        try {
            UUID.fromString(missionRequest.getDroneId());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Mission request droneId must be a UUID", exception);
        }
    }
}

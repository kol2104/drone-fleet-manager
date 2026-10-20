package com.example.drone.mission.service;

import com.example.drone.mission.event.MissionEventType;
import com.example.drone.mission.event.MissionEventRepository;
import com.example.drone.mission.mapper.MissionEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DroneEventLoggingService {

    private final MissionEventRepository missionEventRepository;
    private final MissionEventMapper missionEventMapper;

    public String logEvent(String droneId, MissionEventType eventType, String description) {
        var event = missionEventMapper.toEvent(droneId, eventType, description);
        missionEventRepository.save(event);
        log.info("Mission event stored: type={}, droneId={}", eventType.name(), droneId);
        return event.getEventId();
    }

    public String logEvent(
            String eventId,
            String droneId,
            MissionEventType eventType,
            String description) {
        var event = missionEventMapper.toEvent(eventId, droneId, eventType, description);
        missionEventRepository.save(event);
        log.info("Mission event stored: type={}, droneId={}, eventId={}",
                eventType.name(), droneId, event.getEventId());
        return event.getEventId();
    }
}

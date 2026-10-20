package com.example.drone.mission.event;

public enum MissionEventType {
    MISSION_STARTED(MissionEventSeverity.INFO),
    MISSION_COMPLETED(MissionEventSeverity.INFO),
    MISSION_FAILED(MissionEventSeverity.ERROR),
    WEATHER_CHECK_OK(MissionEventSeverity.INFO),
    WEATHER_CHECK_FAILED(MissionEventSeverity.WARNING),
    ALLOCATED(MissionEventSeverity.INFO),
    ALLOCATION_REJECTED(MissionEventSeverity.WARNING),
    FLIGHT_COMPLETED(MissionEventSeverity.INFO),
    FLIGHT_FAILED(MissionEventSeverity.ERROR),
    FLIGHT_COMMAND_SENT(MissionEventSeverity.INFO),
    DRONE_RELEASED(MissionEventSeverity.INFO);

    private final MissionEventSeverity severity;

    MissionEventType(MissionEventSeverity severity) {
        this.severity = severity;
    }

    public MissionEventSeverity getSeverity() {
        return severity;
    }
}

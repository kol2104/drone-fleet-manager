package com.example.drone.mission.weather;

import com.example.drone.common.dto.WaypointDto;
import java.util.List;

public interface WeatherCheckClient {
    boolean isSafeToFly(List<WaypointDto> waypoints);
}

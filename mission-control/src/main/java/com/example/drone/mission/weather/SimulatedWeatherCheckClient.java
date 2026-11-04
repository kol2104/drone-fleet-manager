package com.example.drone.mission.weather;

import com.example.drone.common.dto.WaypointDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SimulatedWeatherCheckClient implements WeatherCheckClient {

    @Override
    public boolean isSafeToFly(List<WaypointDto> waypoints) {
        // TODO: Replace with a weather-provider client that evaluates the mission route.
        return true;
    }
}

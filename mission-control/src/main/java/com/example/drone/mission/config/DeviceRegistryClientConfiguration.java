package com.example.drone.mission.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class DeviceRegistryClientConfiguration {

    @Bean
    public RestClient deviceRegistryRestClient(
            RestClient.Builder builder,
            @Value("${services.device-registry.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}

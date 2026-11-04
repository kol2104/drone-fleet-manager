package com.example.drone.mission.config;

import io.camunda.zeebe.client.ZeebeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ZeebeClientConfig {

    @Value("${zeebe.client.broker.gateway-address}")
    private String gatewayAddress;

    @Bean
    public ZeebeClient zeebeClient() {
        log.info("Connecting to Zeebe broker at {}", gatewayAddress);

        return ZeebeClient.newClientBuilder()
                .gatewayAddress(gatewayAddress)
                .usePlaintext()
                .build();
    }
}

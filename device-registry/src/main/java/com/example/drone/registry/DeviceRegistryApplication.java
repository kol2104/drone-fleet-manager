package com.example.drone.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DeviceRegistryApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeviceRegistryApplication.class, args);
    }
}

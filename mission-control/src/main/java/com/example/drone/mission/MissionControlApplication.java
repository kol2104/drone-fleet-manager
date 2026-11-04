package com.example.drone.mission;

import io.camunda.zeebe.client.ZeebeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class MissionControlApplication {

    private final ZeebeClient zeebeClient;

    public static void main(String[] args) {
        SpringApplication.run(MissionControlApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    @ConditionalOnProperty(name = "mission-control.startup.enabled", havingValue = "true", matchIfMissing = true)
    public void initializeWorkers() {
        log.info("Mission Control Service started");
        deployProcessDefinition();
        log.info("Mission process is deployed; job workers are managed by Spring configuration");
    }

    private void deployProcessDefinition() {
        final int maxAttempts = 24;
        final long retryDelayMs = 5000;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                zeebeClient.newDeployResourceCommand()
                        .addResourceFromClasspath("bpmn/drone-flight-mission.bpmn20.xml")
                        .send()
                        .join();
                log.info("BPMN process deployed: bpmn/drone-flight-mission.bpmn20.xml");
                return;
            } catch (Exception ex) {
                if (attempt == maxAttempts) {
                    throw ex;
                }
                log.warn("Zeebe deploy attempt {}/{} failed: {}", attempt, maxAttempts, ex.getMessage());
                log.warn("Retrying in {} seconds...", retryDelayMs / 1000);
                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for Zeebe to become ready", ie);
                }
            }
        }
    }

}

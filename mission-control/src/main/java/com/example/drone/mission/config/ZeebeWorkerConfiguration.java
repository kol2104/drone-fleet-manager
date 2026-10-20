package com.example.drone.mission.config;

import com.example.drone.mission.worker.AuditLogWorker;
import com.example.drone.mission.worker.DroneAllocationWorker;
import com.example.drone.mission.worker.DroneReleaseWorker;
import com.example.drone.mission.worker.FlightExecutionWorker;
import com.example.drone.mission.worker.MissionValidationWorker;
import com.example.drone.mission.worker.WeatherCheckWorker;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZeebeWorkerConfiguration {

    @Bean(destroyMethod = "close")
    public JobWorker missionValidationJobWorker(
            ZeebeClient zeebeClient,
            MissionValidationWorker worker) {
        return zeebeClient.newWorker()
                .jobType("validate-mission")
                .handler(worker::handle)
                .open();
    }

    @Bean(destroyMethod = "close")
    public JobWorker weatherCheckJobWorker(
            ZeebeClient zeebeClient,
            WeatherCheckWorker worker) {
        return zeebeClient.newWorker()
                .jobType("check-weather")
                .handler(worker::handleWeatherCheck)
                .open();
    }

    @Bean(destroyMethod = "close")
    public JobWorker droneAllocationJobWorker(
            ZeebeClient zeebeClient,
            DroneAllocationWorker worker) {
        return zeebeClient.newWorker()
                .jobType("allocate-drone")
                .handler(worker::handleDroneAllocation)
                .open();
    }

    @Bean(destroyMethod = "close")
    public JobWorker flightExecutionJobWorker(
            ZeebeClient zeebeClient,
            FlightExecutionWorker worker) {
        return zeebeClient.newWorker()
                .jobType("execute-flight")
                .handler(worker::handleFlightExecution)
                .open();
    }

    @Bean(destroyMethod = "close")
    public JobWorker auditLogJobWorker(
            ZeebeClient zeebeClient,
            AuditLogWorker worker) {
        return zeebeClient.newWorker()
                .jobType("audit-mission-log")
                .handler(worker::handle)
                .open();
    }

    @Bean(destroyMethod = "close")
    public JobWorker droneReleaseJobWorker(
            ZeebeClient zeebeClient,
            DroneReleaseWorker worker) {
        return zeebeClient.newWorker()
                .jobType("release-drone")
                .handler(worker::handle)
                .open();
    }

}

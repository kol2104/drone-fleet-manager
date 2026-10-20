package com.example.drone.mission;

import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class BpmnProcessDefinitionTest {

    @Test
    void missionProcessContainsAllExecutableWorkers() throws Exception {
        try (InputStream process = getClass().getResourceAsStream("/bpmn/drone-flight-mission.bpmn20.xml")) {
            assertThat(process).isNotNull();

            var document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(process);

            var taskDefinitions = document.getElementsByTagName("zeebe:taskDefinition");
            Set<String> jobTypes = IntStream.range(0, taskDefinitions.getLength())
                    .mapToObj(taskDefinitions::item)
                    .map(node -> node.getAttributes().getNamedItem("type").getNodeValue())
                    .collect(Collectors.toSet());

            assertThat(jobTypes).containsExactlyInAnyOrder(
                    "validate-mission",
                    "check-weather",
                    "allocate-drone",
                    "execute-flight",
                    "release-drone",
                    "audit-mission-log"
            );
        }
    }

    @Test
    void missionProcessHasExpectedIdentityAndRetryPolicy() throws Exception {
        try (InputStream process = getClass().getResourceAsStream("/bpmn/drone-flight-mission.bpmn20.xml")) {
            assertThat(process).isNotNull();

            var document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(process);

            var missionProcess = document.getElementsByTagName("bpmn:process").item(0);
            assertThat(missionProcess.getAttributes().getNamedItem("id").getNodeValue())
                    .isEqualTo("drone-flight-mission");
            assertThat(missionProcess.getAttributes().getNamedItem("isExecutable").getNodeValue())
                    .isEqualTo("true");

            var taskDefinitions = document.getElementsByTagName("zeebe:taskDefinition");
            assertThat(IntStream.range(0, taskDefinitions.getLength())
                    .mapToObj(taskDefinitions::item)
                    .map(node -> node.getAttributes().getNamedItem("retries").getNodeValue())
                    .toList())
                    .containsExactly("1", "3", "5", "2", "3", "3");

            assertThat(document.getElementsByTagName("bpmn:eventBasedGateway").getLength()).isEqualTo(1);
            assertThat(document.getElementsByTagName("bpmn:exclusiveGateway").getLength()).isEqualTo(2);
            assertThat(document.getElementsByTagName("bpmn:messageEventDefinition").getLength()).isEqualTo(1);
            assertThat(document.getElementsByTagName("bpmn:timerEventDefinition").getLength()).isEqualTo(1);
            assertThat(document.getElementsByTagName("zeebe:subscription").item(0)
                    .getAttributes().getNamedItem("correlationKey").getNodeValue())
                    .isEqualTo("=flightCorrelationId");

            var weatherFailureFlow = IntStream.range(0, document.getElementsByTagName("bpmn:sequenceFlow").getLength())
                    .mapToObj(document.getElementsByTagName("bpmn:sequenceFlow")::item)
                    .filter(node -> node.getAttributes().getNamedItem("id").getNodeValue()
                            .equals("WeatherRejectedToAudit"))
                    .findFirst()
                    .orElseThrow();
            assertThat(weatherFailureFlow.getAttributes().getNamedItem("targetRef").getNodeValue())
                    .isEqualTo("AuditLogTask");
            assertThat(weatherFailureFlow.getTextContent()).contains("weatherOk = false");

            var allocationFailureFlow = IntStream.range(0, document.getElementsByTagName("bpmn:sequenceFlow").getLength())
                    .mapToObj(document.getElementsByTagName("bpmn:sequenceFlow")::item)
                    .filter(node -> node.getAttributes().getNamedItem("id").getNodeValue()
                            .equals("AllocationRejectedToAudit"))
                    .findFirst()
                    .orElseThrow();
            assertThat(allocationFailureFlow.getAttributes().getNamedItem("targetRef").getNodeValue())
                    .isEqualTo("AuditLogTask");
            assertThat(allocationFailureFlow.getTextContent()).contains("allocationSucceeded = false");
        }
    }
}

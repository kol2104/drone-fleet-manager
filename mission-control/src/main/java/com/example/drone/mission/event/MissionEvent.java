package com.example.drone.mission.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "mission-events", createIndex = true)
public class MissionEvent {

    @Id
    private String eventId;

    @Field(type = FieldType.Keyword)
    private String droneId;

    @Field(type = FieldType.Keyword)
    private String eventType;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private MissionEventSeverity severity;

    @Field(type = FieldType.Keyword)
    private String source;

    @Field(type = FieldType.Date)
    private Instant timestamp;
}

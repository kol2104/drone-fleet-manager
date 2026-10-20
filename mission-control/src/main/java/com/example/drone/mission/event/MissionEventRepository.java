package com.example.drone.mission.event;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionEventRepository extends ElasticsearchRepository<MissionEvent, String> {
}

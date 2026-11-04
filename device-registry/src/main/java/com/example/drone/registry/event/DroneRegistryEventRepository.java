package com.example.drone.registry.event;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DroneRegistryEventRepository extends ElasticsearchRepository<DroneRegistryEvent, String> {
}

package com.example.drone.registry.event;

import com.example.drone.registry.mapper.DroneRegistryEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class DroneRegistryEventListener {

    private final DroneRegistryEventRepository eventRepository;
    private final DroneRegistryEventMapper eventMapper;

    @Async("deviceRegistryAuditExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void persistAfterCommit(DroneRegistryAuditEvent auditEvent) {
        eventRepository.save(eventMapper.toDocument(auditEvent));
        log.debug("Registry audit event persisted: eventId={}, eventType={}, droneId={}",
                auditEvent.eventId(), auditEvent.eventType(), auditEvent.droneId());
    }
}

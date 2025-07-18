package com.talentradar.assessment_service.event.consumer;

import com.talentradar.assessment_service.event.EventType;
import com.talentradar.assessment_service.event.Role;
import com.talentradar.assessment_service.event.UserEvent;
import com.talentradar.assessment_service.model.UserRole;
import com.talentradar.assessment_service.model.UserSnapshot;
import com.talentradar.assessment_service.repository.UserSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {
    private final UserSnapshotRepository userSnapshotRepository;

    @KafkaListener(topics = "user-management-events", groupId = "assessment-service")
    @Transactional
    public void handleUserEvent(
            @Payload Map<String, Object> eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received user event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            log.debug("Event data: {}", eventData);

            UserEvent userEvent = parseUserEvent(eventData);

            // Only process DEVELOPER and MANAGER roles
            if (!isValidRole(userEvent.getRole().name())) {
                log.info("Ignoring user event for role: {}", userEvent.getRole());
                acknowledgment.acknowledge();
                return;
            }

            switch (userEvent.getEventType().name()) {
                case "USER_CREATED":
                case "USER_UPDATED":
                    handleUserCreatedOrUpdated(userEvent);
                    break;
                case "USER_DELETED":
                    handleUserDeleted(userEvent);
                    break;
                default:
                    log.warn("Unknown event type: {}", userEvent.getEventType());
            }

            acknowledgment.acknowledge();
            log.info("Successfully processed user event: {} for user: {}",
                    userEvent.getEventType(), userEvent.getUserId());

        } catch (Exception e) {
            log.error("Error processing user event: {}", e.getMessage(), e);
            // Don't acknowledge - message will be retried
            throw e;
        }
    }

    private UserEvent parseUserEvent(Map<String, Object> eventData) {
        return UserEvent.builder()
                .eventType((EventType) eventData.get("eventType"))
                .userId(UUID.fromString((String) eventData.get("userId")))
                .managerId(eventData.get("managerId") != null ?
                        UUID.fromString((String) eventData.get("managerId")) : null)
                .fullName((String) eventData.get("fullName"))
                .username((String) eventData.get("username"))
                .email((String) eventData.get("email"))
                .role((Role) eventData.get("role"))
                .build();
    }

    private boolean isValidRole(String role) {
        return "DEVELOPER".equals(role) || "MANAGER".equals(role);
    }

    private void handleUserCreatedOrUpdated(UserEvent userEvent) {
        Optional<UserSnapshot> existingSnapshot = userSnapshotRepository.findByUserId(userEvent.getUserId());

        if (existingSnapshot.isPresent()) {
            // Update existing snapshot
            UserSnapshot snapshot = existingSnapshot.get();
            snapshot.setManagerId(userEvent.getManagerId());
            snapshot.setFullName(userEvent.getFullName());
            snapshot.setUsername(userEvent.getUsername());
            snapshot.setEmail(userEvent.getEmail());
            snapshot.setRole(UserRole.valueOf(userEvent.getRole().name()));

            userSnapshotRepository.save(snapshot);
            log.info("Updated user snapshot for userId: {}", userEvent.getUserId());
        } else {
            // Create new snapshot
            UserSnapshot snapshot = UserSnapshot.builder()
                    .userId(userEvent.getUserId())
                    .managerId(userEvent.getManagerId())
                    .fullName(userEvent.getFullName())
                    .username(userEvent.getUsername())
                    .email(userEvent.getEmail())
                    .role(UserRole.valueOf(userEvent.getRole().name()))
                    .build();

            userSnapshotRepository.save(snapshot);
            log.info("Created new user snapshot for userId: {}", userEvent.getUserId());
        }
    }

    private void handleUserDeleted(UserEvent userEvent) {
        Optional<UserSnapshot> existingSnapshot = userSnapshotRepository.findByUserId(userEvent.getUserId());

        if (existingSnapshot.isPresent()) {
            userSnapshotRepository.delete(existingSnapshot.get());
            log.info("Deleted user snapshot for userId: {}", userEvent.getUserId());
        } else {
            log.warn("Attempted to delete non-existent user snapshot for userId: {}", userEvent.getUserId());
        }
    }
}

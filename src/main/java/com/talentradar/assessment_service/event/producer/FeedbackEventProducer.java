package com.talentradar.assessment_service.event.producer;

import com.talentradar.assessment_service.config.KafkaConfig;
import com.talentradar.assessment_service.event.FeedbackEvent;
import com.talentradar.assessment_service.event.FeedbackEventType;
import com.talentradar.assessment_service.event.UserContext;
import com.talentradar.assessment_service.model.Feedback;
import com.talentradar.assessment_service.model.UserSnapshot;
import com.talentradar.assessment_service.repository.UserSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedbackEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserSnapshotRepository userSnapshotRepository;

    public void publishFeedbackCreated(Feedback feedback) {
        publishFeedbackEvent(feedback, FeedbackEventType.FEEDBACK_CREATED);
    }

    public void publishFeedbackUpdated(Feedback feedback) {
        publishFeedbackEvent(feedback, FeedbackEventType.FEEDBACK_UPDATED);
    }

    public void publishFeedbackDeleted(Feedback feedback) {
        publishFeedbackEvent(feedback, FeedbackEventType.FEEDBACK_DELETED);
    }

    public void publishFeedbackVersionCreated(Feedback feedback) {
        publishFeedbackEvent(feedback, FeedbackEventType.FEEDBACK_VERSION_CREATED);
    }

    private void publishFeedbackEvent(Feedback feedback, FeedbackEventType eventType) {
        try {
            UserContext managerContext = getUserContext(feedback.getManagerId());
            UserContext developerContext = getUserContext(feedback.getDeveloperId());

            FeedbackEvent feedbackEvent = FeedbackEvent.builder()
                    .eventType(eventType)
                    .feedbackId(feedback.getId())
                    .managerId(feedback.getManagerId())
                    .developerId(feedback.getDeveloperId())
                    .feedbackVersion(feedback.getFeedbackVersion())
                    .timestamp(LocalDateTime.now())
                    .eventId(UUID.randomUUID().toString())
                    .source("assessment-service")
                    .managerContext(managerContext)
                    .developerContext(developerContext)
                    .build();

            sendFeedbackEvent(feedback, feedbackEvent);

        } catch (Exception e) {
            log.error("Error publishing feedback event {} for feedbackId: {}: {}",
                    eventType, feedback.getId(), e.getMessage(), e);
        }
    }

    private UserContext getUserContext(UUID userId) {
        return userSnapshotRepository.findByUserId(userId)
                .map(userSnapshot -> UserContext.builder()
                        .userId(userSnapshot.getUserId())
                        .fullName(userSnapshot.getFullName())
                        .username(userSnapshot.getUsername())
                        .email(userSnapshot.getEmail())
                        .role(userSnapshot.getRole().name())
                        .managerId(userSnapshot.getManagerId())
                        .build())
                .orElseGet(() -> {
                    log.warn("User snapshot not found for userId: {}", userId);
                    return UserContext.builder()
                            .userId(userId)
                            .fullName("Unknown User")
                            .username("unknown")
                            .email("unknown@example.com")
                            .role("UNKNOWN")
                            .build();
                });
    }

    private void sendFeedbackEvent(Feedback feedback, FeedbackEvent feedbackEvent) {
        // Use feedback ID as the key for partitioning
        String key = feedback.getId().toString();
        
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaConfig.FEEDBACK_EVENTS_TOPIC,
                key,
                feedbackEvent
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent feedback event: {} for feedbackId: {} to topic: {} with offset: {}",
                        feedbackEvent.getEventType(),
                        feedback.getId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send feedback event for feedbackId: {} due to: {}",
                        feedback.getId(), ex.getMessage(), ex);
            }
        });
    }
}
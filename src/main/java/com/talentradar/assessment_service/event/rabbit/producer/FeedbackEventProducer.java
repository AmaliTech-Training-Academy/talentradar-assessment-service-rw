package com.talentradar.assessment_service.event.rabbit.producer;

import com.talentradar.assessment_service.config.RabbitMQConfig;
import com.talentradar.assessment_service.dto.analysis.FeedbackAnalysisDto;
import com.talentradar.assessment_service.event.FeedbackEvent;
import com.talentradar.assessment_service.event.FeedbackEventType;
import com.talentradar.assessment_service.event.UserContext;
import com.talentradar.assessment_service.model.Feedback;
import com.talentradar.assessment_service.model.UserSnapshot;
import com.talentradar.assessment_service.repository.UserSnapshotRepository;
import com.talentradar.assessment_service.service.impl.FeedbackAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedbackEventProducer {
    private final RabbitTemplate rabbitTemplate;
    private final UserSnapshotRepository userSnapshotRepository;
    private final FeedbackAnalysisService feedbackAnalysisService;

    public void publishFeedbackCreated(Feedback feedback) {
        publishFeedbackEvent(feedback, FeedbackEventType.FEEDBACK_CREATED);

        // Also publish feedback.submitted event for analysis
        publishFeedbackSubmittedForAnalysis(feedback);
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

    /**
     * Publishes a feedback.submitted event with combined assessment and feedback data for AI analysis
     */
    private void publishFeedbackSubmittedForAnalysis(Feedback feedback) {
        try {
            log.info("Publishing feedback.submitted event for analysis - feedbackId: {}", feedback.getId());

            // Create the combined analysis DTO
            FeedbackAnalysisDto analysisDto = feedbackAnalysisService.createAnalysisDto(feedback);

            // Send to analysis queue using the feedback.submitted routing key
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ANALYSIS_EVENTS_EXCHANGE,
                    RabbitMQConfig.FEEDBACK_SUBMITTED_KEY,
                    analysisDto
            );

            log.info("Successfully published feedback.submitted event for analysis - userId: {}, feedbackId: {}",
                    analysisDto.getUserId(), feedback.getId());

        } catch (Exception e) {
            log.error("Error publishing feedback.submitted event for analysis - feedbackId: {}: {}",
                    feedback.getId(), e.getMessage(), e);
            // Don't throw exception to avoid breaking the main feedback creation flow
        }
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

            // Send to feedback events exchange
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.FEEDBACK_EVENTS_EXCHANGE,
                    getRoutingKey(eventType),
                    feedbackEvent
            );

            // Also send to notification service if it's a creation event
            if (eventType == FeedbackEventType.FEEDBACK_CREATED) {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.NOTIFICATION_EVENTS_EXCHANGE,
                        RabbitMQConfig.FEEDBACK_CREATED_KEY,
                        createNotificationEvent(feedbackEvent, developerContext)
                );
            }

            log.info("Successfully published feedback event: {} for feedbackId: {}",
                    eventType, feedback.getId());

        } catch (Exception e) {
            log.error("Error publishing feedback event {} for feedbackId: {}: {}",
                    eventType, feedback.getId(), e.getMessage(), e);
        }
    }

    private String getRoutingKey(FeedbackEventType eventType) {
        return switch (eventType) {
            case FEEDBACK_CREATED -> "feedback.created";
            case FEEDBACK_UPDATED -> "feedback.updated";
            case FEEDBACK_DELETED -> "feedback.deleted";
            case FEEDBACK_VERSION_CREATED -> "feedback.version.created";
        };
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

    private Object createNotificationEvent(FeedbackEvent feedbackEvent, UserContext developerContext) {
        // Create a notification event for the notification service
        return new Object() {
            public String getTitle() { return "New Feedback Received"; }
            public String getContent() { return "You have received new feedback from your manager."; }
            public String getRecipientId() { return developerContext.getUserId().toString(); }
            public String getRecipientEmail() { return developerContext.getEmail(); }
            public String getType() { return "IN_APP"; }
            public String getCategory() { return "INFO"; }
        };
    }
}
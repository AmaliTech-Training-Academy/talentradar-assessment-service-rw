package com.talentradar.assessment_service.event.rabbit.producer;

import com.talentradar.assessment_service.config.RabbitMQConfig;
import com.talentradar.assessment_service.event.AssessmentEvent;
import com.talentradar.assessment_service.event.AssessmentEventType;
import com.talentradar.assessment_service.event.UserContext;
import com.talentradar.assessment_service.model.Assessment;
import com.talentradar.assessment_service.model.UserSnapshot;
import com.talentradar.assessment_service.repository.UserSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssessmentEventProducer {
    private final RabbitTemplate rabbitTemplate;
    private final UserSnapshotRepository userSnapshotRepository;

    public void publishAssessmentSubmitted(Assessment assessment) {
        try {
            UserSnapshot userSnapshot = userSnapshotRepository.findByUserId(assessment.getUserId())
                    .orElseThrow(() -> new RuntimeException("User snapshot not found for userId: " + assessment.getUserId()));

            UserContext userContext = UserContext.builder()
                    .userId(userSnapshot.getUserId())
                    .fullName(userSnapshot.getFullName())
                    .username(userSnapshot.getUsername())
                    .email(userSnapshot.getEmail())
                    .role(userSnapshot.getRole().name())
                    .managerId(userSnapshot.getManagerId())
                    .build();

            AssessmentEvent assessmentEvent = AssessmentEvent.builder()
                    .eventType(AssessmentEventType.ASSESSMENT_SUBMITTED)
                    .assessmentId(assessment.getId())
                    .userId(assessment.getUserId())
                    .reflection(assessment.getReflection())
                    .averageScore(assessment.getAverageScore())
                    .submissionStatus(assessment.getSubmissionStatus().name())
                    .timestamp(LocalDateTime.now())
                    .eventId(UUID.randomUUID().toString())
                    .source("assessment-service")
                    .userContext(userContext)
                    .build();

            // Send to assessment events exchange
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ASSESSMENT_EVENTS_EXCHANGE,
                    RabbitMQConfig.ASSESSMENT_SUBMITTED_KEY,
                    assessmentEvent
            );

            // Also send to notification service for the manager
            if (userContext.getManagerId() != null) {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.NOTIFICATION_EVENTS_EXCHANGE,
                        RabbitMQConfig.ASSESSMENT_SUBMITTED_KEY,
                        createNotificationEvent(assessmentEvent, userContext)
                );
            }

            log.info("Successfully published assessment event for assessmentId: {}", assessment.getId());

        } catch (Exception e) {
            log.error("Error publishing assessment event for assessmentId: {}: {}",
                    assessment.getId(), e.getMessage(), e);
        }
    }

    public void publishAssessmentUpdated(Assessment assessment) {
        try {
            UserSnapshot userSnapshot = userSnapshotRepository.findByUserId(assessment.getUserId())
                    .orElseThrow(() -> new RuntimeException("User snapshot not found for userId: " + assessment.getUserId()));

            UserContext userContext = UserContext.builder()
                    .userId(userSnapshot.getUserId())
                    .fullName(userSnapshot.getFullName())
                    .username(userSnapshot.getUsername())
                    .email(userSnapshot.getEmail())
                    .role(userSnapshot.getRole().name())
                    .managerId(userSnapshot.getManagerId())
                    .build();

            AssessmentEvent assessmentEvent = AssessmentEvent.builder()
                    .eventType(AssessmentEventType.ASSESSMENT_UPDATED)
                    .assessmentId(assessment.getId())
                    .userId(assessment.getUserId())
                    .reflection(assessment.getReflection())
                    .averageScore(assessment.getAverageScore())
                    .submissionStatus(assessment.getSubmissionStatus().name())
                    .timestamp(LocalDateTime.now())
                    .eventId(UUID.randomUUID().toString())
                    .source("assessment-service")
                    .userContext(userContext)
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ASSESSMENT_EVENTS_EXCHANGE,
                    "assessment.updated",
                    assessmentEvent
            );

            log.info("Successfully published assessment updated event for assessmentId: {}", assessment.getId());

        } catch (Exception e) {
            log.error("Error publishing assessment updated event for assessmentId: {}: {}",
                    assessment.getId(), e.getMessage(), e);
        }
    }

    private Object createNotificationEvent(AssessmentEvent assessmentEvent, UserContext userContext) {
        return new Object() {
            public String getTitle() { return "New Assessment Submitted"; }
            public String getContent() { 
                return String.format("%s has submitted a new self-assessment.", userContext.getFullName()); 
            }
            public String getRecipientId() { 
                return userContext.getManagerId() != null ? userContext.getManagerId().toString() : null; 
            }
            public String getRecipientEmail() { return "manager@example.com"; } // You'd get this from manager's snapshot
            public String getType() { return "IN_APP"; }
            public String getCategory() { return "INFO"; }
        };
    }
}
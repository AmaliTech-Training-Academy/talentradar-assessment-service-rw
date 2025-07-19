package com.talentradar.assessment_service.event.producer;

import com.talentradar.assessment_service.config.KafkaConfig;
import com.talentradar.assessment_service.event.AssessmentEvent;
import com.talentradar.assessment_service.event.AssessmentEventType;
import com.talentradar.assessment_service.event.UserContext;
import com.talentradar.assessment_service.model.Assessment;
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
public class AssessmentEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
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
                    .eventType(AssessmentEventType.valueOf("ASSESSMENT_SUBMITTED"))
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

            sendAssessment(assessment, assessmentEvent);

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
                    .eventType(AssessmentEventType.valueOf("ASSESSMENT_UPDATED"))
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

            sendAssessment(assessment, assessmentEvent);

        } catch (Exception e) {
            log.error("Error publishing assessment updated event for assessmentId: {}: {}",
                    assessment.getId(), e.getMessage(), e);
        }
    }

    private void sendAssessment(Assessment assessment, AssessmentEvent assessmentEvent) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaConfig.ASSESSMENT_EVENTS_TOPIC,
                assessment.getUserId().toString(),
                assessmentEvent
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent assessment event: {} for assessmentId: {} to topic: {} with offset: {}",
                        assessmentEvent.getEventType(),
                        assessment.getId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send assessment event for assessmentId: {} due to: {}",
                        assessment.getId(), ex.getMessage(), ex);
            }
        });
    }
}
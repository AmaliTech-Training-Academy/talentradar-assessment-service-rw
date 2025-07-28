package com.talentradar.assessment_service.service;

import static org.mockito.Mockito.*;

import com.talentradar.assessment_service.config.RabbitMQConfig;
import com.talentradar.assessment_service.event.AssessmentEvent;
import com.talentradar.assessment_service.event.AssessmentEventType;
import com.talentradar.assessment_service.event.rabbit.producer.AssessmentEventProducer;
import com.talentradar.assessment_service.model.Assessment;
import com.talentradar.assessment_service.model.SubmissionStatus;
import com.talentradar.assessment_service.model.UserRole;
import com.talentradar.assessment_service.model.UserSnapshot;
import com.talentradar.assessment_service.repository.UserSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class AssessmentEventProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private UserSnapshotRepository userSnapshotRepository;

    @InjectMocks
    private AssessmentEventProducer assessmentEventProducer;

    private Assessment testAssessment;
    private UserSnapshot testUserSnapshot;

    @BeforeEach
    void setUp() {
        // Create a test assessment - this is our main input data
        testAssessment = createTestAssessment();

        // Create a test user snapshot - this is what the repository should return
        testUserSnapshot = createTestUserSnapshot();
    }

    // TEST 1: Happy path - everything works perfectly
    @Test
    void publishAssessmentSubmitted_ShouldSucceed_WhenUserSnapshotExists() {
        // ARRANGE: Set up what we expect to happen
        when(userSnapshotRepository.findByUserId(testAssessment.getUserId()))
                .thenReturn(Optional.of(testUserSnapshot));

        // ACT: Call the method we're testing
        assessmentEventProducer.publishAssessmentSubmitted(testAssessment);

        // ASSERT: Verify the right things happened
        // 1. Repository was called to find the user
        verify(userSnapshotRepository).findByUserId(testAssessment.getUserId());

        // 2. Assessment event was sent to the main exchange
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.ASSESSMENT_EVENTS_EXCHANGE),
                eq(RabbitMQConfig.ASSESSMENT_SUBMITTED_KEY),
                any(AssessmentEvent.class)
        );

        // 3. Notification was sent to manager (since our test user has a manager)
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EVENTS_EXCHANGE),
                eq(RabbitMQConfig.ASSESSMENT_SUBMITTED_KEY),
                any(Object.class)
        );

        // Total: 2 messages should be sent
        verify(rabbitTemplate, times(2)).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }

    // TEST 2: Edge case - user has no manager
    @Test
    void publishAssessmentSubmitted_ShouldSkipNotification_WhenUserHasNoManager() {
        // ARRANGE: Create user without a manager
        UserSnapshot userWithoutManager = createTestUserSnapshot();
        userWithoutManager.setManagerId(null); // No manager!

        when(userSnapshotRepository.findByUserId(testAssessment.getUserId()))
                .thenReturn(Optional.of(userWithoutManager));

        // ACT
        assessmentEventProducer.publishAssessmentSubmitted(testAssessment);

        // ASSERT: Only main event should be sent, no notification
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.ASSESSMENT_EVENTS_EXCHANGE),
                eq(RabbitMQConfig.ASSESSMENT_SUBMITTED_KEY),
                any(AssessmentEvent.class)
        );

        // Verify NO notification was sent
        verify(rabbitTemplate, never()).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EVENTS_EXCHANGE),
                anyString(),
                any(Object.class)
        );

        // Total: only 1 message sent
        verify(rabbitTemplate, times(1)).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }

    // TEST 3: Error case - user not found
    @Test
    void publishAssessmentSubmitted_ShouldHandleError_WhenUserSnapshotNotFound() {
        // ARRANGE: Repository returns empty (user not found)
        when(userSnapshotRepository.findByUserId(testAssessment.getUserId()))
                .thenReturn(Optional.empty());

        // ACT: This should not throw an exception (it's caught internally)
        assessmentEventProducer.publishAssessmentSubmitted(testAssessment);

        // ASSERT: Repository was called but no messages sent due to error
        verify(userSnapshotRepository).findByUserId(testAssessment.getUserId());
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }

    // TEST 4: Error case - repository throws exception
    @Test
    void publishAssessmentSubmitted_ShouldHandleError_WhenRepositoryThrowsException() {
        // ARRANGE: Repository throws an exception
        when(userSnapshotRepository.findByUserId(testAssessment.getUserId()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // ACT: Should not crash the application
        assessmentEventProducer.publishAssessmentSubmitted(testAssessment);

        // ASSERT: No messages sent due to error
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }

    // TEST 5: Error case - RabbitMQ fails
    @Test
    void publishAssessmentSubmitted_ShouldHandleError_WhenRabbitTemplateThrowsException() {
        // ARRANGE: User exists but RabbitMQ fails
        when(userSnapshotRepository.findByUserId(testAssessment.getUserId()))
                .thenReturn(Optional.of(testUserSnapshot));

        doThrow(new RuntimeException("RabbitMQ connection failed"))
                .when(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class));

        // ACT: Should not crash
        assessmentEventProducer.publishAssessmentSubmitted(testAssessment);

        // ASSERT: Repository was called (that part worked)
        verify(userSnapshotRepository).findByUserId(testAssessment.getUserId());
    }

    // TEST 6: Test the updated method - happy path
    @Test
    void publishAssessmentUpdated_ShouldSucceed_WhenUserSnapshotExists() {
        // ARRANGE
        when(userSnapshotRepository.findByUserId(testAssessment.getUserId()))
                .thenReturn(Optional.of(testUserSnapshot));

        // ACT
        assessmentEventProducer.publishAssessmentUpdated(testAssessment);

        // ASSERT: Only sends to main exchange (no notification for updates)
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.ASSESSMENT_EVENTS_EXCHANGE),
                eq("assessment.updated"), // Different routing key
                any(AssessmentEvent.class)
        );

        // Only 1 message sent (no manager notification for updates)
        verify(rabbitTemplate, times(1)).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }

    // TEST 7: Test updated method - user not found
    @Test
    void publishAssessmentUpdated_ShouldHandleError_WhenUserSnapshotNotFound() {
        // ARRANGE
        when(userSnapshotRepository.findByUserId(testAssessment.getUserId()))
                .thenReturn(Optional.empty());

        // ACT
        assessmentEventProducer.publishAssessmentUpdated(testAssessment);

        // ASSERT: No messages sent
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }

    // TEST 8: Verify the content of the assessment event
    @Test
    void publishAssessmentSubmitted_ShouldCreateCorrectAssessmentEvent() {
        // ARRANGE
        when(userSnapshotRepository.findByUserId(testAssessment.getUserId()))
                .thenReturn(Optional.of(testUserSnapshot));

        // ACT
        assessmentEventProducer.publishAssessmentSubmitted(testAssessment);

        // ASSERT: Capture and verify the event content
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.ASSESSMENT_EVENTS_EXCHANGE),
                eq(RabbitMQConfig.ASSESSMENT_SUBMITTED_KEY),
                argThat((AssessmentEvent event) -> event.getEventType() == AssessmentEventType.ASSESSMENT_SUBMITTED
                        && event.getAssessmentId().equals(testAssessment.getId())
                        && event.getUserId().equals(testAssessment.getUserId())
                        && event.getSource().equals("assessment-service")
                        && event.getUserContext() != null
                        && event.getUserContext().getFullName().equals(testUserSnapshot.getFullName()))
        );

    }

    // Helper methods to create test data
    private Assessment createTestAssessment() {
        Assessment assessment = new Assessment();
        assessment.setId(UUID.randomUUID());
        assessment.setUserId(UUID.randomUUID());
        assessment.setReflection("This is my reflection");
        assessment.setAverageScore(4);
        assessment.setSubmissionStatus(SubmissionStatus.SUBMITTED);
        return assessment;
    }

    private UserSnapshot createTestUserSnapshot() {
        UserSnapshot snapshot = new UserSnapshot();
        snapshot.setUserId(testAssessment.getUserId()); // Use same userId as assessment
        snapshot.setFullName("Ganza Kevin");
        snapshot.setUsername("ganzaKevin");
        snapshot.setEmail("ganza.kevin@example.com");
        snapshot.setRole(UserRole.DEVELOPER);
        snapshot.setManagerId(UUID.randomUUID()); // Has a manager
        return snapshot;
    }
}
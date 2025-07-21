//package com.talentradar.assessment_service.service;
//
////import com.talentradar.assessment_service.config.KafkaConfig;
//import com.talentradar.assessment_service.event.FeedbackEvent;
//import com.talentradar.assessment_service.event.FeedbackEventType;
//import com.talentradar.assessment_service.event.UserContext;
//import com.talentradar.assessment_service.event.producer.FeedbackEventProducer;
//import com.talentradar.assessment_service.model.Feedback;
//import com.talentradar.assessment_service.model.UserRole;
//import com.talentradar.assessment_service.model.UserSnapshot;
//import com.talentradar.assessment_service.repository.UserSnapshotRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.support.SendResult;
//
//import java.util.Optional;
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("FeedbackEventProducer Tests")
//class FeedbackEventProducerTest {
//
//    @Mock
//    private KafkaTemplate<String, Object> kafkaTemplate;
//
//    @Mock
//    private UserSnapshotRepository userSnapshotRepository;
//
//    @Mock
//    private SendResult<String, Object> sendResult;
//
//    @InjectMocks
//    private FeedbackEventProducer feedbackEventProducer;
//
//    private UUID feedbackId;
//    private UUID managerId;
//    private UUID developerId;
//    private Feedback sampleFeedback;
//    private UserSnapshot managerSnapshot;
//    private UserSnapshot developerSnapshot;
//
//    @BeforeEach
//    void setUp() {
//        feedbackId = UUID.randomUUID();
//        managerId = UUID.randomUUID();
//        developerId = UUID.randomUUID();
//
//        sampleFeedback = Feedback.builder()
//                .id(feedbackId)
//                .managerId(managerId)
//                .developerId(developerId)
//                .feedbackVersion(1)
//                .build();
//
//        managerSnapshot = UserSnapshot.builder()
//                .userId(managerId)
//                .fullName("John Manager")
//                .username("john.manager")
//                .email("john.manager@example.com")
//                .role(UserRole.MANAGER)
//                .build();
//
//        developerSnapshot = UserSnapshot.builder()
//                .userId(developerId)
//                .fullName("Jane Developer")
//                .username("jane.developer")
//                .email("jane.developer@example.com")
//                .role(UserRole.DEVELOPER)
//                .managerId(managerId)
//                .build();
//    }
//
//    @Test
//    @DisplayName("Should publish feedback created event successfully")
//    void publishFeedbackCreated_ShouldPublishEvent_WhenValidFeedback() {
//        // Given
//        when(userSnapshotRepository.findByUserId(managerId)).thenReturn(Optional.of(managerSnapshot));
//        when(userSnapshotRepository.findByUserId(developerId)).thenReturn(Optional.of(developerSnapshot));
//
//        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
//        when(kafkaTemplate.send(eq(KafkaConfig.FEEDBACK_EVENTS_TOPIC), eq(feedbackId.toString()), any(FeedbackEvent.class)))
//                .thenReturn(future);
//
//        // When
//        feedbackEventProducer.publishFeedbackCreated(sampleFeedback);
//
//        // Then
//        ArgumentCaptor<FeedbackEvent> eventCaptor = ArgumentCaptor.forClass(FeedbackEvent.class);
//        verify(kafkaTemplate).send(eq(KafkaConfig.FEEDBACK_EVENTS_TOPIC), eq(feedbackId.toString()), eventCaptor.capture());
//
//        FeedbackEvent capturedEvent = eventCaptor.getValue();
//        assertThat(capturedEvent.getEventType()).isEqualTo(FeedbackEventType.FEEDBACK_CREATED);
//        assertThat(capturedEvent.getFeedbackId()).isEqualTo(feedbackId);
//        assertThat(capturedEvent.getManagerId()).isEqualTo(managerId);
//        assertThat(capturedEvent.getDeveloperId()).isEqualTo(developerId);
//        assertThat(capturedEvent.getFeedbackVersion()).isEqualTo(1);
//        assertThat(capturedEvent.getSource()).isEqualTo("assessment-service");
//        assertThat(capturedEvent.getEventId()).isNotNull();
//        assertThat(capturedEvent.getTimestamp()).isNotNull();
//
//        // Verify manager context
//        UserContext managerContext = capturedEvent.getManagerContext();
//        assertThat(managerContext.getUserId()).isEqualTo(managerId);
//        assertThat(managerContext.getFullName()).isEqualTo("John Manager");
//        assertThat(managerContext.getUsername()).isEqualTo("john.manager");
//        assertThat(managerContext.getEmail()).isEqualTo("john.manager@example.com");
//        assertThat(managerContext.getRole()).isEqualTo("MANAGER");
//
//        // Verify developer context
//        UserContext developerContext = capturedEvent.getDeveloperContext();
//        assertThat(developerContext.getUserId()).isEqualTo(developerId);
//        assertThat(developerContext.getFullName()).isEqualTo("Jane Developer");
//        assertThat(developerContext.getUsername()).isEqualTo("jane.developer");
//        assertThat(developerContext.getEmail()).isEqualTo("jane.developer@example.com");
//        assertThat(developerContext.getRole()).isEqualTo("DEVELOPER");
//        assertThat(developerContext.getManagerId()).isEqualTo(managerId);
//
//        verify(userSnapshotRepository).findByUserId(managerId);
//        verify(userSnapshotRepository).findByUserId(developerId);
//    }
//
//    @Test
//    @DisplayName("Should publish feedback updated event successfully")
//    void publishFeedbackUpdated_ShouldPublishEvent_WhenValidFeedback() {
//        // Given
//        when(userSnapshotRepository.findByUserId(managerId)).thenReturn(Optional.of(managerSnapshot));
//        when(userSnapshotRepository.findByUserId(developerId)).thenReturn(Optional.of(developerSnapshot));
//
//        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
//        when(kafkaTemplate.send(eq(KafkaConfig.FEEDBACK_EVENTS_TOPIC), eq(feedbackId.toString()), any(FeedbackEvent.class)))
//                .thenReturn(future);
//
//        // When
//        feedbackEventProducer.publishFeedbackUpdated(sampleFeedback);
//
//        // Then
//        ArgumentCaptor<FeedbackEvent> eventCaptor = ArgumentCaptor.forClass(FeedbackEvent.class);
//        verify(kafkaTemplate).send(eq(KafkaConfig.FEEDBACK_EVENTS_TOPIC), eq(feedbackId.toString()), eventCaptor.capture());
//
//        FeedbackEvent capturedEvent = eventCaptor.getValue();
//        assertThat(capturedEvent.getEventType()).isEqualTo(FeedbackEventType.FEEDBACK_UPDATED);
//        assertThat(capturedEvent.getFeedbackId()).isEqualTo(feedbackId);
//        assertThat(capturedEvent.getManagerId()).isEqualTo(managerId);
//        assertThat(capturedEvent.getDeveloperId()).isEqualTo(developerId);
//    }
//
//    @Test
//    @DisplayName("Should publish feedback deleted event successfully")
//    void publishFeedbackDeleted_ShouldPublishEvent_WhenValidFeedback() {
//        // Given
//        when(userSnapshotRepository.findByUserId(managerId)).thenReturn(Optional.of(managerSnapshot));
//        when(userSnapshotRepository.findByUserId(developerId)).thenReturn(Optional.of(developerSnapshot));
//
//        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
//        when(kafkaTemplate.send(eq(KafkaConfig.FEEDBACK_EVENTS_TOPIC), eq(feedbackId.toString()), any(FeedbackEvent.class)))
//                .thenReturn(future);
//
//        // When
//        feedbackEventProducer.publishFeedbackDeleted(sampleFeedback);
//
//        // Then
//        ArgumentCaptor<FeedbackEvent> eventCaptor = ArgumentCaptor.forClass(FeedbackEvent.class);
//        verify(kafkaTemplate).send(eq(KafkaConfig.FEEDBACK_EVENTS_TOPIC), eq(feedbackId.toString()), eventCaptor.capture());
//
//        FeedbackEvent capturedEvent = eventCaptor.getValue();
//        assertThat(capturedEvent.getEventType()).isEqualTo(FeedbackEventType.FEEDBACK_DELETED);
//        assertThat(capturedEvent.getFeedbackId()).isEqualTo(feedbackId);
//    }
//
//    @Test
//    @DisplayName("Should publish feedback version created event successfully")
//    void publishFeedbackVersionCreated_ShouldPublishEvent_WhenValidFeedback() {
//        // Given
//        when(userSnapshotRepository.findByUserId(managerId)).thenReturn(Optional.of(managerSnapshot));
//        when(userSnapshotRepository.findByUserId(developerId)).thenReturn(Optional.of(developerSnapshot));
//
//        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
//        when(kafkaTemplate.send(eq(KafkaConfig.FEEDBACK_EVENTS_TOPIC), eq(feedbackId.toString()), any(FeedbackEvent.class)))
//                .thenReturn(future);
//
//        // When
//        feedbackEventProducer.publishFeedbackVersionCreated(sampleFeedback);
//
//        // Then
//        ArgumentCaptor<FeedbackEvent> eventCaptor = ArgumentCaptor.forClass(FeedbackEvent.class);
//        verify(kafkaTemplate).send(eq(KafkaConfig.FEEDBACK_EVENTS_TOPIC), eq(feedbackId.toString()), eventCaptor.capture());
//
//        FeedbackEvent capturedEvent = eventCaptor.getValue();
//        assertThat(capturedEvent.getEventType()).isEqualTo(FeedbackEventType.FEEDBACK_VERSION_CREATED);
//        assertThat(capturedEvent.getFeedbackId()).isEqualTo(feedbackId);
//    }
//
//    @Test
//    @DisplayName("Should handle missing user snapshot gracefully")
//    void publishFeedbackCreated_ShouldHandleMissingUserSnapshot_WhenUserNotFound() {
//        // Given
//        when(userSnapshotRepository.findByUserId(managerId)).thenReturn(Optional.empty());
//        when(userSnapshotRepository.findByUserId(developerId)).thenReturn(Optional.of(developerSnapshot));
//
//        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
//        when(kafkaTemplate.send(eq(KafkaConfig.FEEDBACK_EVENTS_TOPIC), eq(feedbackId.toString()), any(FeedbackEvent.class)))
//                .thenReturn(future);
//
//        // When
//        feedbackEventProducer.publishFeedbackCreated(sampleFeedback);
//
//        // Then
//        ArgumentCaptor<FeedbackEvent> eventCaptor = ArgumentCaptor.forClass(FeedbackEvent.class);
//        verify(kafkaTemplate).send(eq(KafkaConfig.FEEDBACK_EVENTS_TOPIC), eq(feedbackId.toString()), eventCaptor.capture());
//
//        FeedbackEvent capturedEvent = eventCaptor.getValue();
//
//        // Verify manager context has default values
//        UserContext managerContext = capturedEvent.getManagerContext();
//        assertThat(managerContext.getUserId()).isEqualTo(managerId);
//        assertThat(managerContext.getFullName()).isEqualTo("Unknown User");
//        assertThat(managerContext.getUsername()).isEqualTo("unknown");
//        assertThat(managerContext.getEmail()).isEqualTo("unknown@example.com");
//        assertThat(managerContext.getRole()).isEqualTo("UNKNOWN");
//
//        // Verify developer context is still correct
//        UserContext developerContext = capturedEvent.getDeveloperContext();
//        assertThat(developerContext.getUserId()).isEqualTo(developerId);
//        assertThat(developerContext.getFullName()).isEqualTo("Jane Developer");
//    }
//
//    @Test
//    @DisplayName("Should not fail when Kafka send fails")
//    void publishFeedbackCreated_ShouldNotFail_WhenKafkaSendFails() {
//        // Given
//        when(userSnapshotRepository.findByUserId(managerId)).thenReturn(Optional.of(managerSnapshot));
//        when(userSnapshotRepository.findByUserId(developerId)).thenReturn(Optional.of(developerSnapshot));
//
//        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
//        failedFuture.completeExceptionally(new RuntimeException("Kafka send failed"));
//        when(kafkaTemplate.send(eq(KafkaConfig.FEEDBACK_EVENTS_TOPIC), eq(feedbackId.toString()), any(FeedbackEvent.class)))
//                .thenReturn(failedFuture);
//
//        // When & Then - Should not throw exception
//        assertThatCode(() -> feedbackEventProducer.publishFeedbackCreated(sampleFeedback))
//                .doesNotThrowAnyException();
//
//        verify(kafkaTemplate).send(eq(KafkaConfig.FEEDBACK_EVENTS_TOPIC), eq(feedbackId.toString()), any(FeedbackEvent.class));
//    }
//}
package com.talentradar.assessment_service.service;

import com.rabbitmq.client.Channel;
import com.talentradar.assessment_service.event.EventType;
import com.talentradar.assessment_service.event.Role;
import com.talentradar.assessment_service.event.UserEvent;
import com.talentradar.assessment_service.event.rabbit.consumer.UserEventConsumer;
import com.talentradar.assessment_service.model.UserRole;
import com.talentradar.assessment_service.model.UserSnapshot;
import com.talentradar.assessment_service.repository.UserSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEventConsumerTest {

    @Mock
    private UserSnapshotRepository userSnapshotRepository;

    @Mock
    private Channel channel;

    @Mock
    private Message message;

    @Mock
    private MessageProperties messageProperties;

    @InjectMocks
    private UserEventConsumer userEventConsumer;

    private UserEvent testUserEvent;
    private UserSnapshot existingUserSnapshot;
    private final long DELIVERY_TAG = 123L;

    @BeforeEach
    void setUp() {
        // Create test user event
        testUserEvent = createTestUserEvent(EventType.USER_CREATED);

        // Create existing user snapshot for update scenarios
        existingUserSnapshot = createExistingUserSnapshot();

        // Setup message mocks
        when(message.getMessageProperties()).thenReturn(messageProperties);
        when(messageProperties.getDeliveryTag()).thenReturn(DELIVERY_TAG);
    }

    // TEST 1: Happy path - USER_CREATED event creates new snapshot
    @Test
    void handleUserEvent_ShouldCreateNewSnapshot_WhenUserCreatedAndUserNotExists() throws Exception {
        // ARRANGE: No existing user snapshot
        when(userSnapshotRepository.findByUserId(testUserEvent.getUserId()))
                .thenReturn(Optional.empty());

        // ACT
        userEventConsumer.handleUserEvent(testUserEvent, message, channel);

        // ASSERT
        // 1. Repository was queried
        verify(userSnapshotRepository).findByUserId(testUserEvent.getUserId());

        // 2. New snapshot was saved
        verify(userSnapshotRepository).save(argThat(snapshot ->
                snapshot.getUserId().equals(testUserEvent.getUserId()) &&
                        snapshot.getFullName().equals(testUserEvent.getFullName()) &&
                        snapshot.getUsername().equals(testUserEvent.getUsername()) &&
                        snapshot.getEmail().equals(testUserEvent.getEmail()) &&
                        snapshot.getRole() == UserRole.valueOf(testUserEvent.getRole().name()) &&
                        snapshot.getManagerId().equals(testUserEvent.getManagerId())
        ));

        // 3. Message was acknowledged
        verify(channel).basicAck(DELIVERY_TAG, false);

        // 4. No nack was sent
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    // TEST 2: Happy path - USER_UPDATED event updates existing snapshot
    @Test
    void handleUserEvent_ShouldUpdateSnapshot_WhenUserUpdatedAndUserExists() throws Exception {
        // ARRANGE: User already exists
        UserEvent updateEvent = createTestUserEvent(EventType.USER_UPDATED);
        when(userSnapshotRepository.findByUserId(updateEvent.getUserId()))
                .thenReturn(Optional.of(existingUserSnapshot));

        // ACT
        userEventConsumer.handleUserEvent(updateEvent, message, channel);

        // ASSERT
        // 1. Repository was queried
        verify(userSnapshotRepository).findByUserId(updateEvent.getUserId());

        // 2. Existing snapshot was updated and saved
        verify(userSnapshotRepository).save(argThat(snapshot ->
                snapshot.getId().equals(existingUserSnapshot.getId()) && // Same ID (update)
                        snapshot.getUserId().equals(existingUserSnapshot.getUserId()) && // Same userId
                        snapshot.getFullName().equals(updateEvent.getFullName()) // Updated data
        ));

        // 3. Message was acknowledged
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    // TEST 3: Happy path - USER_CREATED event updates existing snapshot (duplicate event)
    @Test
    void handleUserEvent_ShouldUpdateSnapshot_WhenUserCreatedButUserAlreadyExists() throws Exception {
        // ARRANGE: User already exists (maybe duplicate CREATE event)
        when(userSnapshotRepository.findByUserId(testUserEvent.getUserId()))
                .thenReturn(Optional.of(existingUserSnapshot));

        // ACT
        userEventConsumer.handleUserEvent(testUserEvent, message, channel);

        // ASSERT
        // Should update existing snapshot instead of creating new one
        verify(userSnapshotRepository).save(any(UserSnapshot.class));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    // TEST 4: Happy path - USER_DELETED event deletes existing snapshot
    @Test
    void handleUserEvent_ShouldDeleteSnapshot_WhenUserDeletedAndUserExists() throws Exception {
        // ARRANGE
        UserEvent deleteEvent = createTestUserEvent(EventType.USER_DELETED);
        when(userSnapshotRepository.findByUserId(deleteEvent.getUserId()))
                .thenReturn(Optional.of(existingUserSnapshot));

        // ACT
        userEventConsumer.handleUserEvent(deleteEvent, message, channel);

        // ASSERT
        // 1. Repository was queried
        verify(userSnapshotRepository).findByUserId(deleteEvent.getUserId());

        // 2. Snapshot was deleted
        verify(userSnapshotRepository).delete(existingUserSnapshot);

        // 3. Message was acknowledged
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    // TEST 5: Edge case - USER_DELETED event when user doesn't exist
    @Test
    void handleUserEvent_ShouldHandleGracefully_WhenUserDeletedButUserNotExists() throws Exception {
        // ARRANGE: User doesn't exist
        UserEvent deleteEvent = createTestUserEvent(EventType.USER_DELETED);
        when(userSnapshotRepository.findByUserId(deleteEvent.getUserId()))
                .thenReturn(Optional.empty());

        // ACT
        userEventConsumer.handleUserEvent(deleteEvent, message, channel);

        // ASSERT
        // 1. Repository was queried
        verify(userSnapshotRepository).findByUserId(deleteEvent.getUserId());

        // 2. No delete was called (user didn't exist)
        verify(userSnapshotRepository, never()).delete(any());

        // 3. Message was still acknowledged (it's not an error)
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    // TEST 6: Error case - Repository throws exception during CREATE/UPDATE
    @Test
    void handleUserEvent_ShouldNackMessage_WhenRepositoryThrowsException() throws Exception {
        // ARRANGE: Repository throws exception
        when(userSnapshotRepository.findByUserId(testUserEvent.getUserId()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // ACT
        userEventConsumer.handleUserEvent(testUserEvent, message, channel);

        // ASSERT
        // 1. Message should be nacked and requeued
        verify(channel).basicNack(DELIVERY_TAG, false, true);

        // 2. No ack should be sent
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    // TEST 7: Error case - Repository throws exception during save
    @Test
    void handleUserEvent_ShouldNackMessage_WhenSaveThrowsException() throws Exception {
        // ARRANGE: Find works but save fails
        when(userSnapshotRepository.findByUserId(testUserEvent.getUserId()))
                .thenReturn(Optional.empty());
        when(userSnapshotRepository.save(any(UserSnapshot.class)))
                .thenThrow(new RuntimeException("Save failed"));

        // ACT
        userEventConsumer.handleUserEvent(testUserEvent, message, channel);

        // ASSERT
        verify(channel).basicNack(DELIVERY_TAG, false, true);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    // TEST 8: Error case - Repository throws exception during delete
    @Test
    void handleUserEvent_ShouldNackMessage_WhenDeleteThrowsException() throws Exception {
        // ARRANGE
        UserEvent deleteEvent = createTestUserEvent(EventType.USER_DELETED);
        when(userSnapshotRepository.findByUserId(deleteEvent.getUserId()))
                .thenReturn(Optional.of(existingUserSnapshot));
        doThrow(new RuntimeException("Delete failed"))
                .when(userSnapshotRepository).delete(existingUserSnapshot);

        // ACT
        userEventConsumer.handleUserEvent(deleteEvent, message, channel);

        // ASSERT
        verify(channel).basicNack(DELIVERY_TAG, false, true);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    // TEST 9: Error case - Channel operations fail
    @Test
    void handleUserEvent_ShouldHandleChannelErrors_WhenNackFails() throws Exception {
        // ARRANGE: Repository fails AND nack fails
        when(userSnapshotRepository.findByUserId(testUserEvent.getUserId()))
                .thenThrow(new RuntimeException("Database failed"));
        doThrow(new RuntimeException("Channel failed"))
                .when(channel).basicNack(anyLong(), anyBoolean(), anyBoolean());

        // ACT: Should not throw exception (error is logged)
        userEventConsumer.handleUserEvent(testUserEvent, message, channel);

        // ASSERT: Verify both operations were attempted
        verify(channel).basicNack(DELIVERY_TAG, false, true);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    // TEST 10: Verify correct data mapping for user creation
    @Test
    void handleUserEvent_ShouldMapDataCorrectly_WhenCreatingUser() {
        // ARRANGE
        when(userSnapshotRepository.findByUserId(testUserEvent.getUserId()))
                .thenReturn(Optional.empty());

        // ACT
        userEventConsumer.handleUserEvent(testUserEvent, message, channel);

        // ASSERT: Verify all fields are mapped correctly
        verify(userSnapshotRepository).save(argThat(snapshot -> snapshot.getUserId().equals(testUserEvent.getUserId()) &&
                snapshot.getFullName().equals(testUserEvent.getFullName()) &&
                snapshot.getUsername().equals(testUserEvent.getUsername()) &&
                snapshot.getEmail().equals(testUserEvent.getEmail()) &&
                snapshot.getManagerId().equals(testUserEvent.getManagerId()) &&
                snapshot.getRole().name().equals(testUserEvent.getRole().name())));
    }

    // Helper methods to create test data
    private UserEvent createTestUserEvent(EventType eventType) {
        return UserEvent.builder()
                .eventType(eventType)
                .userId(UUID.randomUUID())
                .fullName("Ganza Kevin")
                .username("ganzaKevin")
                .email("ganza.kevin@example.com")
                .role(Role.DEVELOPER)
                .managerId(UUID.randomUUID())
                .build();
    }

    private UserSnapshot createExistingUserSnapshot() {
        UserSnapshot snapshot = new UserSnapshot();
        snapshot.setId(UUID.randomUUID());
        snapshot.setUserId(testUserEvent.getUserId()); // Same as test event
        snapshot.setFullName("Gwiza Kelly");
        snapshot.setUsername("gwizaKelly");
        snapshot.setEmail("gwiza.kelly@example.com");
        snapshot.setRole(UserRole.DEVELOPER);
        snapshot.setManagerId(UUID.randomUUID());
        return snapshot;
    }
}

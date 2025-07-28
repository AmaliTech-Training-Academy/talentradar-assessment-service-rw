package com.talentradar.assessment_service.event.rabbit.consumer;

import com.rabbitmq.client.Channel;
import com.talentradar.assessment_service.config.RabbitMQConfig;
import com.talentradar.assessment_service.event.UserEvent;
import com.talentradar.assessment_service.model.UserRole;
import com.talentradar.assessment_service.model.UserSnapshot;
import com.talentradar.assessment_service.repository.UserSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {


    private final UserSnapshotRepository userSnapshotRepository;

    // Single listener method for the queue - handles all user events
    @RabbitListener(queues = RabbitMQConfig.USER_UPDATED_KEY)
    @RabbitListener(queues = RabbitMQConfig.USER_CREATED_KEY)
    @Transactional
    public void handleUserEvent(UserEvent userEvent, Message message, Channel channel) {
        try {
            log.info("👍 Received user event: {} for user: {}", userEvent.getEventType(), userEvent.getUserId());
            log.debug("Event data: {}", userEvent);

            // Handle different event types
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

            // Acknowledge the message
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info("Successfully processed user event: {} for user: {}",
                    userEvent.getEventType(), userEvent.getUserId());

        } catch (Exception e) {
            log.error("Error processing user event: {}", e.getMessage(), e);
            try {
                // Reject and requeue the message
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            } catch (Exception nackError) {
                log.error("Failed to nack message: {}", nackError.getMessage());
            }
        }
    }

    private void handleUserCreatedOrUpdated(UserEvent userEvent) {
        try {
            log.info("Processing user created/updated event for userId: {}", userEvent.getUserId());

            Optional<UserSnapshot> existingSnapshot = userSnapshotRepository.findByUserId(userEvent.getUserId());

            if (existingSnapshot.isPresent()) {
                // Update existing snapshot
                UserSnapshot snapshot = existingSnapshot.get();
                snapshot.setManagerId(existingSnapshot.get().getManagerId());
                snapshot.setFullName(userEvent.getFullName());
                snapshot.setUsername(userEvent.getUsername());
                snapshot.setRole(UserRole.valueOf(existingSnapshot.get().getRole().name()));
                snapshot.setId(existingSnapshot.get().getId());
                snapshot.setUserId(existingSnapshot.get().getUserId());
                snapshot.setEmail(existingSnapshot.get().getEmail());

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
        } catch (Exception e) {
            log.error("Error handling user created/updated: {}", e.getMessage(), e);
            throw e; // Re-throw to trigger message requeue
        }
    }

    private void handleUserDeleted(UserEvent userEvent) {
        try {
            log.info("Processing user deleted event for userId: {}", userEvent.getUserId());

            Optional<UserSnapshot> existingSnapshot = userSnapshotRepository.findByUserId(userEvent.getUserId());

            if (existingSnapshot.isPresent()) {
                userSnapshotRepository.delete(existingSnapshot.get());
                log.info("Deleted user snapshot for userId: {}", userEvent.getUserId());
            } else {
                log.warn("Attempted to delete non-existent user snapshot for userId: {}", userEvent.getUserId());
            }
        } catch (Exception e) {
            log.error("Error handling user deleted: {}", e.getMessage(), e);
            throw e; // Re-throw to trigger message requeue
        }
    }
}
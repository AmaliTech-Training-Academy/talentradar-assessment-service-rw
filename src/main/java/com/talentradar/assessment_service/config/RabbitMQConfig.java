package com.talentradar.assessment_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMQConfig {

    // Exchange names
    public static final String USER_EVENTS_EXCHANGE = "user.events.exchange";
    public static final String ASSESSMENT_EVENTS_EXCHANGE = "assessment.events.exchange";
    public static final String FEEDBACK_EVENTS_EXCHANGE = "feedback.events.exchange";
    public static final String NOTIFICATION_EVENTS_EXCHANGE = "notification.events.exchange";

    // Queue names
    public static final String USER_EVENTS_QUEUE = "user.events.queue";
    public static final String ASSESSMENT_EVENTS_QUEUE = "assessment.events.queue";
    public static final String FEEDBACK_EVENTS_QUEUE = "feedback.events.queue";
    public static final String FEEDBACK_SUBMITTED_QUEUE = "feedback.submitted.queue";
    public static final String ASSESSMENT_SUBMITTED_QUEUE = "assessment.submitted.queue";

    // Routing keys
    public static final String USER_CREATED_KEY = "user-created";
    public static final String USER_UPDATED_KEY = "user-updated";
    public static final String USER_DELETED_KEY = "user-deleted";
    public static final String ASSESSMENT_SUBMITTED_KEY = "assessment.submitted";
    public static final String FEEDBACK_CREATED_KEY = "feedback.created";
    public static final String FEEDBACK_UPDATED_KEY = "feedback.updated";
    public static final String FEEDBACK_DELETED_KEY = "feedback.deleted";

    // Message converter
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate for sending messages
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        // Enable publisher confirms
        template.setMandatory(true);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("Message sent successfully with correlation: {}", correlationData);
            } else {
                log.error("Message failed to send with correlation: {}, cause: {}", correlationData, cause);
            }
        });
        template.setReturnsCallback((returned) -> {
            log.error("Message returned: {}", returned.getMessage());
        });
        return template;
    }

    // Listener container factory
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setErrorHandler(new ConditionalRejectingErrorHandler());
        return factory;
    }

    // ============= EXCHANGES =============

    @Bean
    public TopicExchange userEventsExchange() {
        return ExchangeBuilder.topicExchange(USER_EVENTS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange assessmentEventsExchange() {
        return ExchangeBuilder.topicExchange(ASSESSMENT_EVENTS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange feedbackEventsExchange() {
        return ExchangeBuilder.topicExchange(FEEDBACK_EVENTS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange notificationEventsExchange() {
        return ExchangeBuilder.topicExchange(NOTIFICATION_EVENTS_EXCHANGE)
                .durable(true)
                .build();
    }

    // ============= QUEUES =============

    @Bean
    public Queue userEventsQueue() {
        return QueueBuilder.durable(USER_EVENTS_QUEUE)
                .build();
    }

    @Bean
    public Queue assessmentEventsQueue() {
        return QueueBuilder.durable(ASSESSMENT_EVENTS_QUEUE)
                .build();
    }

    @Bean
    public Queue feedbackEventsQueue() {
        return QueueBuilder.durable(FEEDBACK_EVENTS_QUEUE)
                .build();
    }

    @Bean
    public Queue feedbackSubmittedQueue() {
        return QueueBuilder.durable(FEEDBACK_SUBMITTED_QUEUE)
                .build();
    }

    @Bean
    public Queue assessmentSubmittedQueue() {
        return QueueBuilder.durable(ASSESSMENT_SUBMITTED_QUEUE)
                .build();
    }

    // ============= BINDINGS =============

    @Bean
    public Binding userEventsBinding() {
        return BindingBuilder.bind(userEventsQueue())
                .to(userEventsExchange())
                .with("user.*");
    }

    @Bean
    public Binding assessmentEventsBinding() {
        return BindingBuilder.bind(assessmentEventsQueue())
                .to(assessmentEventsExchange())
                .with("assessment.*");
    }

    @Bean
    public Binding feedbackEventsBinding() {
        return BindingBuilder.bind(feedbackEventsQueue())
                .to(feedbackEventsExchange())
                .with("feedback.*");
    }

    @Bean
    public Binding feedbackSubmittedBinding() {
        return BindingBuilder.bind(feedbackSubmittedQueue())
                .to(notificationEventsExchange())
                .with(FEEDBACK_CREATED_KEY);
    }

    @Bean
    public Binding assessmentSubmittedBinding() {
        return BindingBuilder.bind(assessmentSubmittedQueue())
                .to(notificationEventsExchange())
                .with(ASSESSMENT_SUBMITTED_KEY);
    }
}
package com.example.drone.mission.config;

import com.example.drone.common.messaging.DroneFlightMessaging;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DroneFlightMessagingConfiguration {

    public static final String DEAD_LETTER_EXCHANGE = "drone.flight.dlx";

    @Bean
    public TopicExchange droneFlightExchange() {
        return new TopicExchange(DroneFlightMessaging.EXCHANGE_NAME, true, false);
    }

    @Bean
    public TopicExchange droneFlightDeadLetterExchange() {
        return new TopicExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue droneFlightCommandQueue() {
        return QueueBuilder.durable(DroneFlightMessaging.COMMAND_QUEUE)
                .maxLength(100_000L)
                .build();
    }

    @Bean
    public Queue droneFlightResultQueue() {
        return QueueBuilder.durable(DroneFlightMessaging.RESULT_QUEUE)
                .maxLength(100_000L)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(DroneFlightMessaging.RESULT_DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    public Queue droneFlightResultDeadLetterQueue() {
        return QueueBuilder.durable(DroneFlightMessaging.RESULT_DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding droneFlightCommandBinding(
            @Qualifier("droneFlightCommandQueue") Queue queue,
            @Qualifier("droneFlightExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(DroneFlightMessaging.COMMAND_ROUTING_KEY_PATTERN);
    }

    @Bean
    public Binding droneFlightResultBinding(
            @Qualifier("droneFlightResultQueue") Queue queue,
            @Qualifier("droneFlightExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(DroneFlightMessaging.RESULT_ROUTING_KEY_PATTERN);
    }

    @Bean
    public Binding droneFlightResultDeadLetterBinding(
            @Qualifier("droneFlightResultDeadLetterQueue") Queue queue,
            @Qualifier("droneFlightDeadLetterExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(DroneFlightMessaging.RESULT_DEAD_LETTER_QUEUE);
    }
}

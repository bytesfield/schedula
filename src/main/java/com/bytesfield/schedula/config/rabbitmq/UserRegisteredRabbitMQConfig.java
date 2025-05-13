package com.bytesfield.schedula.config.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserRegisteredRabbitMQConfig {

    public static final String QUEUE = "user.queue";
    public static final String EXCHANGE = "user.exchange";
    public static final String ROUTING_KEY = "user.registered";

    @Bean
    public Binding userRegisteredBinding(Queue userQueue, DirectExchange userExchange) {
        return BindingBuilder.bind(userQueue).to(userExchange).with(ROUTING_KEY);
    }

    @Bean
    public Queue userQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(EXCHANGE);
    }
}

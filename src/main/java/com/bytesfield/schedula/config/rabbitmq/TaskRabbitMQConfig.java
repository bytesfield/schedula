package com.bytesfield.schedula.config.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskRabbitMQConfig {

    public static final String QUEUE = "task.queue";
    public static final String EXCHANGE = "task.exchange";
    public static final String ROUTING_KEY = "task.schedule";

    @Bean
    public Binding binding(Queue taskQueue, DirectExchange taskExchange) {
        return BindingBuilder.bind(taskQueue).to(taskExchange).with(ROUTING_KEY);
    }

    @Bean
    public Queue taskQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public DirectExchange taskExchange() {
        return new DirectExchange(EXCHANGE);
    }
}
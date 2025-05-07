package com.bytesfield.schedula.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue taskQueue() {
        return new Queue("task_queue", true);
    }

    @Bean
    public DirectExchange taskExchange() {
        return new DirectExchange("task_exchange");
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(taskQueue())
                .to(taskExchange())
                .with("task.routingkey");
    }
}
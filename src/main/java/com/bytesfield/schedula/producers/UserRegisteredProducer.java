package com.bytesfield.schedula.producers;

import com.bytesfield.schedula.config.rabbitmq.UserRegisteredRabbitMQConfig;
import com.bytesfield.schedula.dtos.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserRegisteredProducer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public UserRegisteredProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendUserRegistered(UserDto userDto) {
        rabbitTemplate.convertAndSend(
                UserRegisteredRabbitMQConfig.EXCHANGE,
                UserRegisteredRabbitMQConfig.ROUTING_KEY,
                userDto
        );

        log.info("User registered sent to queue: {}", userDto);
    }
}

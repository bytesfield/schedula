package com.bytesfield.schedula.producers;

import com.bytesfield.schedula.config.rabbitmq.TaskRabbitMQConfig;
import com.bytesfield.schedula.dtos.requests.TaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ScheduleTaskProducer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public ScheduleTaskProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendTask(TaskResponse task) {
        rabbitTemplate.convertAndSend(
                TaskRabbitMQConfig.EXCHANGE,
                TaskRabbitMQConfig.ROUTING_KEY,
                task
        );

        log.info("Task sent to queue: {}", task);
    }
}

package com.bytesfield.schedula.cron.jobs;

import com.bytesfield.schedula.models.entities.Task;
import com.bytesfield.schedula.models.enums.TaskStatus;
import com.bytesfield.schedula.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskSchedulerJob {

    private final TaskRepository taskRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedRate = 30000) // Check every 30 seconds
    public void scheduleTasks() {
        List<Task> dueTasks = taskRepository.findDueTasks(Instant.now());

        dueTasks.forEach(task -> {
            // Sends it to the message queue
            rabbitTemplate.convertAndSend("taskExchange", "task.routingkey", task);

            task.setStatus(TaskStatus.QUEUED);
            taskRepository.save(task);
        });
    }
}

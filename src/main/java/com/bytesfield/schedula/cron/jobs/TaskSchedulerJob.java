package com.bytesfield.schedula.cron.jobs;

import com.bytesfield.schedula.dtos.requests.TaskResponse;
import com.bytesfield.schedula.models.entities.Task;
import com.bytesfield.schedula.producers.ScheduleTaskProducer;
import com.bytesfield.schedula.repositories.TaskRepository;
import com.bytesfield.schedula.utils.mappers.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskSchedulerJob {

    private final TaskRepository taskRepository;
    private final ScheduleTaskProducer scheduleTaskProducer;
    private final TaskMapper taskMapper;

    @Scheduled(fixedRate = 30000) // Check every 30 seconds
    public void scheduleTasks() {
        List<Task> dueTasks = taskRepository.findDueTasks(Instant.now());

        dueTasks.forEach(task -> {
            TaskResponse taskResponse = taskMapper.toResponse(task);
            
            scheduleTaskProducer.sendTask(taskResponse);

            taskRepository.markAsQueued(task.getId());
        });
    }
}

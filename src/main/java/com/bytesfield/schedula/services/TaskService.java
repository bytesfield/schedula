package com.bytesfield.schedula.services;

import com.bytesfield.schedula.dtos.requests.TaskRequest;
import com.bytesfield.schedula.dtos.requests.TaskResponse;
import com.bytesfield.schedula.exceptions.DefaultException;
import com.bytesfield.schedula.exceptions.InvalidScheduleException;
import com.bytesfield.schedula.exceptions.TaskSchedulingException;
import com.bytesfield.schedula.exceptions.UserNotFoundException;
import com.bytesfield.schedula.models.entities.Task;
import com.bytesfield.schedula.models.enums.TaskStatus;
import com.bytesfield.schedula.models.enums.TaskType;
import com.bytesfield.schedula.repositories.TaskRepository;
import com.bytesfield.schedula.utils.mappers.TaskMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.quartz.CronExpression;
import org.springframework.scheduling.SchedulingException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskScheduler taskScheduler;
    private final TaskMapper taskMapper;
    private final UserService userService;

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        try {
            validateTaskRequest(request);

            Task task = taskMapper.taskRequestToTask(request);
            Task savedTask = taskRepository.save(task);

            scheduleTask(savedTask);

            return taskMapper.taskToTaskResponse(savedTask);
        } catch (UserNotFoundException | InvalidScheduleException | TaskSchedulingException e) {
            throw e;
        } catch (Exception e) {
            throw new DefaultException("Unexpected error while creating task", e);
        }
    }

    private void validateTaskRequest(TaskRequest request) {
        TaskType taskType = request.getType();

        if (isCronTask(taskType) && !isValidCron(request.getCronExpression())) {
            throw new InvalidScheduleException("Invalid cron expression: " + request.getCronExpression());
        }

        boolean isValidTriggerTime = request.getTriggerTime() != null && request.getTriggerTime().isAfter(Instant.now());

        if (isTimestampTask(taskType) && !isValidTriggerTime) {
            throw new InvalidScheduleException("Trigger time must be in the future");

        }

        throw new InvalidScheduleException("Unsupported task type: " + request.getType());
    }

    private void scheduleTask(Task task) {
        try {
            if (isCronTask(task.getType()) && isValidCron(task.getCronExpression())) {
                taskScheduler.schedule(() -> executeTask(task), new CronTrigger(task.getCronExpression()));
            } else if (isTimestampTask(task.getType()) && task.getNextRunAt() != null) {
                taskScheduler.schedule(() -> executeTask(task), task.getNextRunAt());
            }
            task.setStatus(TaskStatus.QUEUED);
        } catch (SchedulingException e) {
            task.setStatus(TaskStatus.FAILED);
            throw new TaskSchedulingException("Failed to schedule task", e);
        }
    }

    private Boolean isCronTask(TaskType type) {
        return "CRON".equalsIgnoreCase(String.valueOf(type));
    }

    private boolean isValidCron(String expression) {
        return CronExpression.isValidExpression(expression);
    }

    private Boolean isTimestampTask(TaskType type) {
        return "TIMESTAMP".equalsIgnoreCase(String.valueOf(type));
    }

    private void executeTask(Task task) {
        // Logic to execute the task


    }
}

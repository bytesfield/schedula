package com.bytesfield.schedula.services;

import com.bytesfield.schedula.dtos.requests.TaskRequest;
import com.bytesfield.schedula.dtos.requests.TaskResponse;
import com.bytesfield.schedula.dtos.requests.UpdateTaskRequest;
import com.bytesfield.schedula.exceptions.DefaultException;
import com.bytesfield.schedula.exceptions.InvalidScheduleException;
import com.bytesfield.schedula.exceptions.ResourceNotFoundException;
import com.bytesfield.schedula.exceptions.UserNotFoundException;
import com.bytesfield.schedula.models.entities.Task;
import com.bytesfield.schedula.models.entities.User;
import com.bytesfield.schedula.models.enums.TaskType;
import com.bytesfield.schedula.producers.ScheduleTaskProducer;
import com.bytesfield.schedula.repositories.TaskRepository;
import com.bytesfield.schedula.utils.TaskHelper;
import com.bytesfield.schedula.utils.mappers.TaskMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserService userService;
    private final ScheduleTaskProducer scheduleTaskProducer;

    @Transactional
    public TaskResponse createTask(UserDetails userDetails, TaskRequest request) {
        try {
            validateTaskRequest(request);

            User user = this.getUserByEmail(userDetails.getUsername());

            Task task = taskMapper.toEntity(request, user);
            Task savedTask = taskRepository.save(task);

            TaskResponse response = taskMapper.toResponse(savedTask);

            scheduleTaskProducer.sendTask(response); //Publish a task to RabbitMQ

            return response;
        } catch (Exception e) {
            log.error("Error while creating task: {}", e.getMessage(), e);

            if (e instanceof InvalidScheduleException) {
                throw new InvalidScheduleException(e.getMessage());
            }

            if (e instanceof UserNotFoundException) {
                throw new UserNotFoundException(e.getMessage());
            }

            throw new DefaultException("Something went wrong. Try again later.", e);
        }
    }

    private void validateTaskRequest(TaskRequest request) {
        TaskType taskType = request.getType();

        boolean isCronTask = TaskHelper.isCronTask(taskType);
        boolean isTimestampTask = TaskHelper.isTimestampTask(taskType);
        boolean isValidTaskType = isCronTask || isTimestampTask;

        if (!isValidTaskType) {
            throw new InvalidScheduleException("Invalid task type: " + taskType);
        }

        if (isCronTask && !TaskHelper.isValidCron(request.getCronExpression())) {
            throw new InvalidScheduleException("Invalid cron expression: " + request.getCronExpression());
        }

        boolean isValidTriggerTime = request.getTriggerTime() != null && request.getTriggerTime().isAfter(Instant.now());

        if (isTimestampTask && !isValidTriggerTime) {
            throw new InvalidScheduleException("Trigger time must be in the future");

        }
    }

    private User getUserByEmail(String email) {
        return userService.getUserByEmail(email);
    }

    public TaskResponse getTask(UserDetails userDetail, int id) {
        Task task = getUserTaskById(userDetail, id);

        return taskMapper.toResponse(task);
    }

    private Task getUserTaskById(UserDetails userDetails, int id) {
        User user = this.getUserByEmail(userDetails.getUsername());

        Task task = taskRepository.findUserTaskById(user, id);

        if (task == null) {
            throw new ResourceNotFoundException("Task not found");
        }

        return task;
    }

    public void deleteTask(UserDetails userDetail, int id) {
        Task task = getUserTaskById(userDetail, id);

        taskRepository.delete(task);
    }

    public TaskResponse updateTask(UserDetails userDetail, int id, UpdateTaskRequest request) {
        Task task = getUserTaskById(userDetail, id);

        taskMapper.updateEntity(request, task);

        Task updatedTask = taskRepository.save(task);

        return taskMapper.toResponse(updatedTask);
    }

    public List<TaskResponse> getUserTasks(UserDetails userDetail) {
        User user = this.getUserByEmail(userDetail.getUsername());

        List<Task> tasks = taskRepository.findUserTasks(user);

        return taskMapper.toResponseList(tasks);
    }
}

package com.bytesfield.schedula.listeners;

import com.bytesfield.schedula.config.rabbitmq.TaskRabbitMQConfig;
import com.bytesfield.schedula.dtos.requests.TaskResponse;
import com.bytesfield.schedula.exceptions.ResourceNotFoundException;
import com.bytesfield.schedula.models.entities.Notification;
import com.bytesfield.schedula.models.entities.Task;
import com.bytesfield.schedula.models.enums.NotificationStatus;
import com.bytesfield.schedula.models.enums.ScheduleType;
import com.bytesfield.schedula.models.enums.TaskStatus;
import com.bytesfield.schedula.models.enums.TaskType;
import com.bytesfield.schedula.repositories.NotificationRepository;
import com.bytesfield.schedula.repositories.TaskRepository;
import com.bytesfield.schedula.services.NotificationService;
import com.bytesfield.schedula.utils.TaskHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.SchedulingException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class ScheduleTaskListener {

    private final NotificationRepository notificationRepository;
    private final TaskScheduler taskScheduler;
    private final NotificationService notificationService;
    private final TaskRepository taskRepository;

    public ScheduleTaskListener(TaskScheduler taskScheduler,
                                NotificationRepository notificationRepository,
                                NotificationService notificationService, TaskRepository taskRepository) {
        this.taskScheduler = taskScheduler;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.taskRepository = taskRepository;

    }

    @RabbitListener(queues = TaskRabbitMQConfig.QUEUE)
    public void listen(TaskResponse taskResponse) {
        int taskId = taskResponse.getId();
        log.info("📩 Received task ID={} for scheduling", taskId);

        Task task = getTask(taskId);

        scheduleTask(task);
    }


    private void scheduleTask(Task task) {
        try {
            TaskType type = task.getType();

            int taskId = task.getId();

            if (TaskHelper.isCronTask(type) && TaskHelper.isValidCron(task.getCronExpression())) {
                scheduleCronTask(task);
            } else if (TaskHelper.isTimestampTask(type) && task.getNextRunAt() != null) {
                scheduleTimestampTask(task);
            } else {
                log.warn("⚠️ Skipping task ID={} due to invalid configuration", taskId);

                taskRepository.markAsFailed(taskId);
                return;
            }

            taskRepository.markAsQueued(taskId);
            log.info("⏳ Task ID={} scheduled successfully", task.getId());

        } catch (SchedulingException e) {
            handleFailure(task, e);
        }
    }

    private void scheduleCronTask(Task task) {
        taskScheduler.schedule(() -> safelyExecuteTask(task), new CronTrigger(task.getCronExpression()));
    }

    private void scheduleTimestampTask(Task task) {
        taskScheduler.schedule(() -> safelyExecuteTask(task), task.getNextRunAt());
    }

    private void safelyExecuteTask(Task task) {
        try {
            executeTask(task);
        } catch (Exception e) {
            handleFailure(task, e);
        }
    }

    private void executeTask(Task task) throws Exception {
        Notification notification = new Notification();

        if (task.getNotificationType() == null) {
            throw new IllegalArgumentException("NotificationType is null for task ID=" + task.getId());
        }

        notification.setTask(task);
        notification.setType(task.getNotificationType());
        notification.setStatus(NotificationStatus.PENDING);
        notification.setAttemptNumber(1);

        Notification savedNotification = notificationRepository.save(notification);

        notificationService.sendNotification(savedNotification); //Sends the Notification

        if (isOneTimeTask(task)) {
            completeTask(task);
            return;
        }

        retryTaskIfNeeded(task);
    }

    private void handleFailure(Task task, Exception e) {
        log.error("❌ Task execution failed: ID={}, Error={}", task.getId(), e.getMessage());

        taskRepository.markAsFailed(task.getId());
    }

    private Task getTask(int id) {
        return taskRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private void retryTaskIfNeeded(Task task) {
        int maxRetries = 3;
        int delayInSeconds = 10;
        int retryCount = task.getRetryCount();

        if (retryCount < maxRetries) {
            task.setStatus(TaskStatus.PROCESSING);
            task.setLastRunAt(task.getNextRunAt());
            task.setRetryCount(retryCount + 1);
            task.setNextRunAt(Instant.now().plusSeconds(delayInSeconds));
            taskRepository.save(task);

            int countLeft = maxRetries - retryCount;

            log.info("✅ Task executed successfully and still processing: ID={}, {} attempt(s) left", task.getId(), countLeft);

            return;
        }

        completeTask(task);
    }

    private boolean isOneTimeTask(Task task) {
        return task.getScheduleType() == ScheduleType.ONCE;
    }

    private void completeTask(Task task) {
        task.setCompleted(true);
        task.setStatus(TaskStatus.COMPLETED);
        taskRepository.save(task);

        log.info("✅ Task executed and completed successfully: ID={}", task.getId());
    }
}

package com.bytesfield.schedula.dtos.requests;

import com.bytesfield.schedula.models.enums.NotificationType;
import com.bytesfield.schedula.models.enums.ScheduleType;
import com.bytesfield.schedula.models.enums.TaskType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class TaskRequest {
    @NotNull(message = "Task type is required")
    private TaskType type;

    @NotNull(message = "NotificationType type is required")
    private NotificationType notificationType;

    @NotNull(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Schedule type is required")
    private ScheduleType scheduleType;

    @Size(max = 50, message = "Cron expression too long")
    @NotNull(message = "cronExpression type is required")
    private String cronExpression;

    @Future(message = "Trigger time must be in the future")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @NotNull(message = "Tigger time type is required")
    private Instant triggerTime;

    @NotNull(message = "Payload is required")
    private Map<String, Object> payload;

    @Min(value = 1, message = "Retries must be at least 1")
    @Max(value = 10, message = "Retries cannot exceed 10")
    private Integer maxRetries = 3;
}

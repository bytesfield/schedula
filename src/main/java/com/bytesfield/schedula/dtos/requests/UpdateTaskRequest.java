package com.bytesfield.schedula.dtos.requests;

import com.bytesfield.schedula.models.enums.NotificationType;
import com.bytesfield.schedula.models.enums.ScheduleType;
import com.bytesfield.schedula.models.enums.TaskType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class UpdateTaskRequest {
    private TaskType type;

    private NotificationType notificationType;

    private String title;

    private String description;

    private ScheduleType scheduleType;

    @Size(max = 50, message = "Cron expression too long")
    private String cronExpression;

    @Future(message = "Trigger time must be in the future")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Instant triggerTime;

    private Map<String, Object> payload;

    @Min(value = 1, message = "Retries must be at least 1")
    @Max(value = 10, message = "Retries cannot exceed 10")
    private Integer maxRetries = 3;
}

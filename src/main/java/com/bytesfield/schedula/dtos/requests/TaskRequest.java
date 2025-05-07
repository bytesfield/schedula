package com.bytesfield.schedula.dtos.requests;

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

    @NotNull(message = "Email type is required")
    private String email;

    @NotNull(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Schedule type is required")
    @Pattern(regexp = "^(CRON|TIMESTAMP)$", message = "Invalid schedule type")
    private String scheduleType;

    @Size(max = 50, message = "Cron expression too long")
    private String cronExpression;

    @Future(message = "Trigger time must be in the future")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Instant triggerTime;

    @NotNull(message = "Payload is required")
    private Map<String, Object> payload;

    @Min(value = 1, message = "Retries must be at least 1")
    @Max(value = 10, message = "Retries cannot exceed 10")
    private Integer maxRetries = 3;
}

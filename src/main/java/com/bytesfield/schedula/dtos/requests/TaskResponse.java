package com.bytesfield.schedula.dtos.requests;

import com.bytesfield.schedula.models.enums.TaskStatus;
import com.bytesfield.schedula.models.enums.TaskType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskResponse {

    private Integer id;
    private TaskType type;
    private String scheduleType;
    private String cronExpression;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Instant triggerTime;

    private Map<String, Object> payload;
    private TaskStatus status;
    private Integer retryCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Instant createdAt;
}
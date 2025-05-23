package com.bytesfield.schedula.utils.mappers;

import com.bytesfield.schedula.dtos.requests.TaskRequest;
import com.bytesfield.schedula.dtos.requests.TaskResponse;
import com.bytesfield.schedula.dtos.requests.UpdateTaskRequest;
import com.bytesfield.schedula.models.entities.Task;
import com.bytesfield.schedula.models.entities.User;
import com.bytesfield.schedula.models.enums.ScheduleType;
import com.bytesfield.schedula.models.enums.TaskStatus;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    default Task toEntity(TaskRequest request, User user) {
        Task task = new Task();

        task.setUser(user);
        task.setTitle(request.getTitle());
        task.setType(request.getType());
        task.setRetryCount(1);
        task.setScheduleType(ScheduleType.valueOf(String.valueOf(request.getScheduleType())));
        task.setCronExpression(request.getCronExpression());
        task.setNextRunAt(request.getTriggerTime());
        task.setData(request.getPayload());
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(Instant.now());

        return task;
    }

    default void updateEntity(UpdateTaskRequest request, Task task) {
        task.setTitle(request.getTitle());
        task.setType(request.getType());
        task.setScheduleType(ScheduleType.valueOf(String.valueOf(request.getScheduleType())));
        task.setCronExpression(request.getCronExpression());
        task.setNextRunAt(request.getTriggerTime());
        task.setData(request.getPayload());
        task.setStatus(TaskStatus.PENDING);
        task.setUpdatedAt(Instant.now());

    }

    default List<TaskResponse> toResponseList(List<Task> tasks) {
        return tasks.stream()
                .map(this::toResponse)
                .toList();
    }

    default TaskResponse toResponse(Task savedTask) {
        TaskResponse taskResponse = new TaskResponse();

        taskResponse.setType(savedTask.getType());
        taskResponse.setId(savedTask.getId());
        taskResponse.setScheduleType(String.valueOf(savedTask.getScheduleType()));
        taskResponse.setCronExpression(savedTask.getCronExpression());
        taskResponse.setTriggerTime(savedTask.getNextRunAt());
        taskResponse.setPayload(savedTask.getData());
        taskResponse.setStatus(savedTask.getStatus());
        taskResponse.setRetryCount(savedTask.getRetryCount());
        taskResponse.setCreatedAt(savedTask.getCreatedAt());

        return taskResponse;
    }

}
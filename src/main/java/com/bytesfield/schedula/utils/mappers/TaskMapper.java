package com.bytesfield.schedula.utils.mappers;

import com.bytesfield.schedula.dtos.requests.TaskRequest;
import com.bytesfield.schedula.dtos.requests.TaskResponse;
import com.bytesfield.schedula.models.entities.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    Task taskRequestToTask(TaskRequest taskRequest);

    @Mapping(source = "user.id", target = "id")
    TaskResponse taskToTaskResponse(Task task);
}
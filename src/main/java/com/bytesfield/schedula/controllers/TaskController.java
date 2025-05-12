package com.bytesfield.schedula.controllers;

import com.bytesfield.schedula.dtos.requests.TaskRequest;
import com.bytesfield.schedula.dtos.requests.TaskResponse;
import com.bytesfield.schedula.dtos.requests.UpdateTaskRequest;
import com.bytesfield.schedula.services.TaskService;
import com.bytesfield.schedula.utils.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@Validated
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@Valid @RequestBody TaskRequest requestBody, @AuthenticationPrincipal UserDetails userDetails) {
        TaskResponse task = taskService.createTask(userDetails, requestBody);

        return ResponseEntity.ok(new ApiResponse<>("Task created and queued successfully.", task));

    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(@PathVariable @Positive int id, @AuthenticationPrincipal UserDetails userDetail) {
        TaskResponse taskResponse = taskService.getTask(userDetail, id);

        return ResponseEntity.ok(new ApiResponse<>("Task retrieved successfully.", taskResponse));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasks(@AuthenticationPrincipal UserDetails userDetail) {
        List<TaskResponse> taskResponse = taskService.getUserTasks(userDetail);

        return ResponseEntity.ok(new ApiResponse<>("Tasks retrieved successfully.", taskResponse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(@PathVariable @Positive int id, @AuthenticationPrincipal UserDetails userDetail, @Valid @RequestBody UpdateTaskRequest taskRequest) {
        TaskResponse taskResponse = taskService.updateTask(userDetail, id, taskRequest);

        return ResponseEntity.ok(new ApiResponse<>("Task updated successfully.", taskResponse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteTask(@PathVariable @Positive int id, @AuthenticationPrincipal UserDetails userDetail) {
        taskService.deleteTask(userDetail, id);

        return ResponseEntity.ok(new ApiResponse<>("Task deleted successfully."));
    }
}

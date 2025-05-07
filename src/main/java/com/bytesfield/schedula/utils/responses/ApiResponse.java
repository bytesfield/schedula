package com.bytesfield.schedula.utils.responses;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter

public class ApiResponse<T> {
    private String message;
    private LocalDateTime timestamp;
    private T data;

    public ApiResponse(String message) {
        this(message, null);
    }

    public ApiResponse(String message, T data) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.data = data;
    }
}


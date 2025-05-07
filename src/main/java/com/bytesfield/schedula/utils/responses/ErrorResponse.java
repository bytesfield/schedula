package com.bytesfield.schedula.utils.responses;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String message;


    public ErrorResponse(int status, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
    }

}


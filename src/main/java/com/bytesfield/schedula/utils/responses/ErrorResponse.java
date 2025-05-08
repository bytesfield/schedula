package com.bytesfield.schedula.utils.responses;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String message;


    public ErrorResponse(int status, String message) {
        this.timestamp = Instant.now();
        this.status = status;
        this.message = message;
    }

}


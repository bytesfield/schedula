package com.bytesfield.schedula.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidScheduleException extends RuntimeException {
    public InvalidScheduleException(String message) {
        super(message);
    }
}


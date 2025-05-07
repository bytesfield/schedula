package com.bytesfield.schedula.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.scheduling.SchedulingException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TaskSchedulingException extends RuntimeException {
    public TaskSchedulingException(String message, SchedulingException e) {
        super(message, e);
    }
}


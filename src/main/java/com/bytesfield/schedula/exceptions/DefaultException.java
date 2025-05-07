package com.bytesfield.schedula.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DefaultException extends RuntimeException {
    public DefaultException(String message, Throwable cause) {
        super(message, cause);
    }
}

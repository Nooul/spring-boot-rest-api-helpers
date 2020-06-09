package com.nooul.apihelpers.springbootrest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND,reason =  "resource not found")
public class NotFoundException extends RuntimeException {
    public NotFoundException(String msg) {
        super(msg);
    }
    public NotFoundException() {}

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
package com.yilinglin10.messageservice.exception;

public class InvalidUserException extends RuntimeException {
    public InvalidUserException(String id) {
        super("invalid access to message with id + " + id);
    }
}

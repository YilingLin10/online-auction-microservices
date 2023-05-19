package com.yilinglin10.authservice.exception;

public class DuplicateUsernameException extends RuntimeException{
    public DuplicateUsernameException(String username) {
        super("username "+ username + " already exists");
    }
}

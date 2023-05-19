package com.yilinglin10.authservice.exception;

public class AuthenticateException extends RuntimeException{
    public AuthenticateException(String error) {
        super(error);
    }
}

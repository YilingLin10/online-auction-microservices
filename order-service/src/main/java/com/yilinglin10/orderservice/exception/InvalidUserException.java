package com.yilinglin10.orderservice.exception;

public class InvalidUserException extends RuntimeException{
    public InvalidUserException(Long orderId) {
        super("User is not allowed to access order with id "+orderId);
    }
}

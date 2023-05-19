package com.yilinglin10.orderservice.controller;

import com.yilinglin10.orderservice.exception.InvalidUserException;
import com.yilinglin10.orderservice.exception.OrderDeadlineDueException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class OrderControllerAdvice {
    // ExceptionHandler for invalid user sending EditItemRequest
    @ExceptionHandler({InvalidUserException.class, OrderDeadlineDueException.class})
    public ResponseEntity<Object> handleInvalidSellerException(RuntimeException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", exception.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}

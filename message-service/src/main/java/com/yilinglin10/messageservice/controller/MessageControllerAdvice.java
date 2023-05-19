package com.yilinglin10.messageservice.controller;

import com.yilinglin10.messageservice.exception.InvalidUserException;
import com.yilinglin10.messageservice.exception.RecipientNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class MessageControllerAdvice {

    @ExceptionHandler({InvalidUserException.class, RecipientNotFoundException.class})
    public ResponseEntity<Object> handleInvalidSellerException(RuntimeException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", exception.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    // ExceptionHandler for SendMessageRequest RequestBody validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException exception) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        response.put("errors", errors);
        return ResponseEntity.badRequest().body(response);
    }
}

package com.yilinglin10.authservice.controller;

import com.yilinglin10.authservice.exception.DuplicateUsernameException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.naming.AuthenticationException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class AuthenticationControllerAdvice {
    // ExceptionHandler for UserProfileDto & LoginRequest RequestBody validation
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

    @ExceptionHandler({DuplicateUsernameException.class, AuthenticationException.class})
    public ResponseEntity<Object> handleDuplicateUsernameException(RuntimeException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", exception.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}

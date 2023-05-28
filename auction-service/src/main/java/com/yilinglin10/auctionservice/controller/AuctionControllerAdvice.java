package com.yilinglin10.auctionservice.controller;

import com.yilinglin10.auctionservice.exception.CannotDeleteAuctionException;
import com.yilinglin10.auctionservice.exception.InvalidPlaceBidRequestException;
import com.yilinglin10.auctionservice.exception.InvalidSellerException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class AuctionControllerAdvice {

    // ExceptionHandler for invalid user sending EditItemRequest
    @ExceptionHandler({InvalidSellerException.class, CannotDeleteAuctionException.class, InvalidPlaceBidRequestException.class, IllegalArgumentException.class})
    public ResponseEntity<Object> handleInvalidSellerException(RuntimeException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", exception.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    // ExceptionHandler for EditUserProfileRequest RequestBody validation
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

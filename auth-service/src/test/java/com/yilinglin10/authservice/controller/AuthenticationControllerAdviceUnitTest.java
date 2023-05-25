package com.yilinglin10.authservice.controller;


import com.yilinglin10.authservice.exception.AuthenticateException;
import com.yilinglin10.authservice.exception.DuplicateUsernameException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockMakers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;


@SpringBootTest
@ActiveProfiles("test")
public class AuthenticationControllerAdviceUnitTest {
    private final AuthenticationControllerAdvice advice = new AuthenticationControllerAdvice();

    @Test
    void handleValidationExceptions_methodArgumentNotValidException_respondsBadRequest() {
        // getBindingResult is a final method of parent class BindException: "withSettings().mockMaker(MockMakers.INLINE)"
        MethodArgumentNotValidException methodArgumentNotValidException = mock(MethodArgumentNotValidException.class, withSettings().mockMaker(MockMakers.INLINE));
        BindingResult bindingResult = mock(BindingResult.class);
        given(bindingResult.getAllErrors()).willReturn(List.of(new FieldError("request", "name", "should not be empty")));
        given(methodArgumentNotValidException.getBindingResult()).willReturn(bindingResult);


        ResponseEntity<Object> response = advice.handleValidationExceptions(methodArgumentNotValidException);

        Assertions.assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assertions.assertTrue(response.getBody().toString().contains("should not be empty"));
    }

    @Test
    void handleDuplicateUsernameException_duplicateUserNameException_respondsBadRequest() {
        DuplicateUsernameException exception = mock(DuplicateUsernameException.class);
        given(exception.getMessage()).willReturn("username already exists");

        ResponseEntity<Object> response = advice.handleDuplicateUsernameException(exception);

        Assertions.assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assertions.assertTrue(response.getBody().toString().contains("username already exists"));
    }

    @Test
    void handleDuplicateUsernameException_authenticationException_respondsBadRequest() {
        AuthenticateException exception = mock(AuthenticateException.class);
        given(exception.getMessage()).willReturn("authenticate exception");

        ResponseEntity<Object> response = advice.handleDuplicateUsernameException(exception);

        Assertions.assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assertions.assertTrue(response.getBody().toString().contains("authenticate exception"));
    }
}
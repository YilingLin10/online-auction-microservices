package com.yilinglin10.authservice.controller;

import com.yilinglin10.authservice.dto.LoginRequest;
import com.yilinglin10.authservice.dto.RegistrationRequest;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Set;

@SpringBootTest
@ActiveProfiles("test")
public class RequestValidatorUnitTest {
    @Autowired
    private LocalValidatorFactoryBean validator;

    @Test
    void validateRegistrationRequest_blankFields_returnViolations() {
        RegistrationRequest request = RegistrationRequest.builder()
                .username("")
                .password("")
                .email("")
                .build();
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);

        Assertions.assertEquals(3, violations.size());
        Assertions.assertTrue(violations.stream().anyMatch(violation -> violation.getMessage().equals("username is mandatory for registration")));
        Assertions.assertTrue(violations.stream().anyMatch(violation -> violation.getMessage().equals("password is mandatory for registration")));
        Assertions.assertTrue(violations.stream().anyMatch(violation -> violation.getMessage().equals("email is mandatory for registration")));
    }

    @Test
    void validateRegistrationRequest_invalidEmail_returnViolation() {
        RegistrationRequest request = RegistrationRequest.builder()
                .username("user")
                .password("password")
                .email("email")
                .build();
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);

        Assertions.assertEquals(1, violations.size());
        Assertions.assertTrue(violations.stream().anyMatch(violation -> violation.getMessage().equals("invalid email")));
    }

    @Test
    void validateRegistrationRequest_invalidPhoneNumber_returnViolation() {
        RegistrationRequest request = RegistrationRequest.builder()
                .username("user")
                .password("password")
                .email("user@gmail.com")
                .phoneNumber("0800010010")
                .build();
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);

        Assertions.assertEquals(1, violations.size());
        Assertions.assertTrue(violations.stream().anyMatch(violation -> violation.getMessage().equals("invalid phone number")));
    }

    @Test
    void validateLoginRequest_blankFields() {
        LoginRequest request = LoginRequest.builder()
                .username("")
                .password("")
                .build();
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        Assertions.assertEquals(2, violations.size());
        Assertions.assertTrue(violations.stream().anyMatch(violation -> violation.getMessage().equals("username is mandatory when logging in")));
        Assertions.assertTrue(violations.stream().anyMatch(violation -> violation.getMessage().equals("password is mandatory when logging in")));
    }
}

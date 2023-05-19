package com.yilinglin10.authservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yilinglin10.authservice.dto.LoginRequest;
import com.yilinglin10.authservice.dto.RegistrationRequest;
import com.yilinglin10.authservice.dto.ValidateTokenResponse;
import com.yilinglin10.authservice.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<Object> registerUserProfile(@Valid @RequestBody RegistrationRequest userProfileDto) {
        Map<String, Object> response = new HashMap<>();
        String result = authenticationService.registerUserProfile(userProfileDto);
        if (result.equals("successful")) {
            response.put("response", result);
            return ResponseEntity.ok(response);
        }else {
            response.put("response", "failed to register a user profile");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest loginRequest) {
        Map<String, Object> response = new HashMap<>();
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            String token = authenticationService.generateToken(loginRequest.getUsername());
            response.put("token", token);
            response.put("response", "successful");
            return ResponseEntity.ok(response);
        }else {
            response.put("response", "incorrect username and password pair");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/validateToken")
    public ValidateTokenResponse validateToken(@RequestParam("token") String token) {
        return authenticationService.validateToken(token);
    }
}

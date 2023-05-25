package com.yilinglin10.authservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yilinglin10.authservice.dto.RegistrationRequest;
import com.yilinglin10.authservice.dto.ValidateTokenResponse;
import com.yilinglin10.authservice.entity.Role;
import com.yilinglin10.authservice.entity.UserProfile;
import com.yilinglin10.authservice.exception.DuplicateUsernameException;
import com.yilinglin10.authservice.repository.RoleRepository;
import com.yilinglin10.authservice.repository.UserProfileRepository;
import com.yilinglin10.authservice.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class AuthenticationService {

    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;

    public String registerUserProfile(RegistrationRequest request) {
        if (userProfileRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException(request.getUsername());
        }
        UserProfile userProfile = mapRequestToEntity(request);
        userProfileRepository.save(userProfile);
        return "successful";
    }

    public String generateToken(String username) {
        UserProfile user = userProfileRepository.findByUsername(username).orElseThrow(()-> null);
        return jwtUtils.generateToken(username, user.getId(), user.getRoles().stream().map(Role::getName).toList());
    }

    public ValidateTokenResponse validateToken(String token){
        jwtUtils.validateToken(token);
        return ValidateTokenResponse.builder()
                .userId(jwtUtils.extractUserId(token))
                .username(jwtUtils.extractUsername(token))
                .authorities(jwtUtils.extractAuthorities(token))
                .build();
    }

    private UserProfile mapRequestToEntity(RegistrationRequest request) {
        Role userRole = roleRepository.findByName("USER").orElseThrow(()-> null);
        UserProfile userProfile = new UserProfile();
        userProfile.setId(request.getId());
        userProfile.setUsername(request.getUsername());
        // save the encoded password in the DB
        userProfile.setPassword(passwordEncoder.encode(request.getPassword()));
        userProfile.setEmail(request.getEmail());
        userProfile.setPhoneNumber(request.getPhoneNumber());
        userProfile.setRoles(List.of(userRole));
        return userProfile;
    }
}

package com.yilinglin10.authservice.service;

import com.yilinglin10.authservice.dto.RegistrationRequest;
import com.yilinglin10.authservice.dto.ValidateTokenResponse;
import com.yilinglin10.authservice.entity.Role;
import com.yilinglin10.authservice.entity.UserProfile;
import com.yilinglin10.authservice.exception.DuplicateUsernameException;
import com.yilinglin10.authservice.repository.RoleRepository;
import com.yilinglin10.authservice.repository.UserProfileRepository;
import com.yilinglin10.authservice.util.JwtUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceUnitTest {
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("registerUserProfile_correctRequest_returnsSuccessful")
    void registerUserProfile_correctRequest_returnsSuccessful() {
        Role userRole = Role.builder()
                .name("USER")
                .build();
        Mockito.when(userProfileRepository.existsByUsername("apple")).thenReturn(false);
        Mockito.when(roleRepository.findByName("USER")).thenReturn(Optional.ofNullable(userRole));

        RegistrationRequest request = RegistrationRequest.builder()
                .username("apple")
                .password("password")
                .email("apple@gmail.com")
                .build();
        String result = authenticationService.registerUserProfile(request);
        Assertions.assertEquals("successful", result);
    }

    @Test
    @DisplayName("registerUserProfile_existingUsername_throwsException")
    void registerUserProfile_existingUsername_throwsException() {
        RegistrationRequest request = RegistrationRequest.builder()
                .username("apple")
                .password("password")
                .email("apple@gmail.com")
                .build();
        Mockito.when(userProfileRepository.existsByUsername(request.getUsername())).thenReturn(true);


        Exception exception = Assertions.assertThrows(DuplicateUsernameException.class, ()-> authenticationService.registerUserProfile(request));
        Assertions.assertTrue(exception.getMessage().contains(request.getUsername()));
    }

    @Test
    @DisplayName("generateToke_loggedInUser_returnsToken")
    void generateToke_loggedInUser_returnsToken() {
        Role userRole = Role.builder()
                .id((long)1)
                .name("USER")
                .build();
        UserProfile user = UserProfile.builder()
                        .id((long) 1)
                        .username("user")
                        .password("password")
                        .email("user@email.com")
                        .roles(List.of(userRole))
                        .build();
        String expectedToken = "token";
        Mockito.when(userProfileRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        Mockito.when(jwtUtils.generateToken(any(), any(), anyList())).thenReturn(expectedToken);

        String actualToken = authenticationService.generateToken(user.getUsername());
        Assertions.assertEquals(expectedToken, actualToken);
    }

    @Test
    @DisplayName("validateToken_validatedToken_returnsValidateTokenResponse")
    void validateToken_validatedToken_returnsValidateTokenResponse() {
        Role userRole = Role.builder()
                .id((long)1)
                .name("USER")
                .build();
        UserProfile user = UserProfile.builder()
                .id((long) 1)
                .username("user")
                .password("password")
                .email("user@email.com")
                .roles(List.of(userRole))
                .build();

        Mockito.when(jwtUtils.extractUsername(any())).thenReturn(user.getUsername());
        Mockito.when(jwtUtils.extractUserId(any())).thenReturn(user.getId());
        Mockito.when(jwtUtils.extractAuthorities(any())).thenReturn(user.getRoles().stream().map(Role::getName).toList());

        ValidateTokenResponse response = authenticationService.validateToken("token");

        Assertions.assertEquals(user.getUsername(), response.getUsername());
        Assertions.assertIterableEquals(user.getRoles().stream().map(Role::getName).toList(), response.getAuthorities());
        Assertions.assertEquals(user.getId(), response.getUserId());
    }
}
package com.yilinglin10.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yilinglin10.authservice.dto.LoginRequest;
import com.yilinglin10.authservice.dto.RegistrationRequest;
import com.yilinglin10.authservice.dto.ValidateTokenResponse;
import com.yilinglin10.authservice.service.AuthenticationService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

//@WebMvcTest(controllers = AuthenticationController.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthenticationControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;
    @MockBean
    private AuthenticationManager authenticationManager;

    private RegistrationRequest registrationRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    public void init() {
        registrationRequest = RegistrationRequest.builder()
                .username("apple")
                .password("password")
                .email("apple@email.com")
                .build();
        loginRequest = LoginRequest.builder()
                .username("apple")
                .password("password")
                .build();
    }

    @Test
    void registerUserProfile_validRequest_returnsSuccessMessage() throws Exception {
        given(authenticationService.registerUserProfile(ArgumentMatchers.any())).willReturn("successful");

        ResultActions response = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void registerUserProfile_badRequest_returnsBadRequest() throws Exception {
        given(authenticationService.registerUserProfile(ArgumentMatchers.any())).willReturn("not successful");

        ResultActions response = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());

    }

    @Test
    void login_validRequest_returnsToken() throws Exception {
        Authentication authentication = mock(UsernamePasswordAuthenticationToken.class);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authenticationManager.authenticate(ArgumentMatchers.any())).willReturn(authentication);
        given(authenticationService.generateToken(ArgumentMatchers.any())).willReturn("token");

        ResultActions response = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("token"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.response").value("successful"));
    }

    @Test
    void login_wrongCredentials_returnsBadRequest() throws Exception {
        Authentication authentication = mock(UsernamePasswordAuthenticationToken.class);
        given(authentication.isAuthenticated()).willReturn(false);
        given(authenticationManager.authenticate(ArgumentMatchers.any())).willReturn(authentication);

        ResultActions response = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.response").value("incorrect username and password pair"));
    }

    @Test
    void validateToken_validToken_returnValidateTokenResponse() throws Exception {
        ValidateTokenResponse validateTokenResponse = ValidateTokenResponse.builder()
                        .userId((long) 1)
                        .username("user")
                        .authorities(List.of("USER"))
                        .build();
        given(authenticationService.validateToken(ArgumentMatchers.any())).willReturn(validateTokenResponse);

        String requestToken = "token";
        ResultActions response = mockMvc.perform(get("/auth/validateToken")
                .param("token", requestToken));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(validateTokenResponse.getUserId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(validateTokenResponse.getUsername()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorities").value(Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorities").value(Matchers.containsInAnyOrder(validateTokenResponse.getAuthorities().get(0))));
    }
}
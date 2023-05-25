package com.yilinglin10.authservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yilinglin10.authservice.dto.LoginRequest;
import com.yilinglin10.authservice.dto.RegistrationRequest;
import com.yilinglin10.authservice.entity.Role;
import com.yilinglin10.authservice.entity.UserProfile;
import com.yilinglin10.authservice.repository.UserProfileRepository;
import com.yilinglin10.authservice.util.JwtUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class AuthServiceApplicationIntegrationTests {
    @Container
    static MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.0-debian");
    @Autowired
    private MockMvc mockMVC;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private JwtUtils jwtUtils;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> mySQLContainer.getJdbcUrl());
        registry.add("spring.datasource.driverClassName", () -> mySQLContainer.getDriverClassName());
        registry.add("spring.datasource.username", () -> mySQLContainer.getUsername());
        registry.add("spring.datasource.password", () -> mySQLContainer.getPassword());
    }

    @Test
    void registerUserProfile_validRequest_returnsSuccessful() throws Exception {
        // given
        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .username("user")
                .password("password")
                .email("user@email.com")
                .build();
        String registrationRequestString = objectMapper.writeValueAsString(registrationRequest);

        // when
        mockMVC.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationRequestString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("successful"));

        // then
        Assertions.assertTrue(userProfileRepository.findByUsername(registrationRequest.getUsername()).isPresent());
    }

    @Test
    void registerUserProfile_blankFields_returnsBadRequestAndErrors() throws Exception {
        // given
        RegistrationRequest registrationRequest = new RegistrationRequest();
        String registrationRequestString = objectMapper.writeValueAsString(registrationRequest);

        // when
        mockMVC.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationRequestString))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").value("username is mandatory for registration"))
                .andExpect(jsonPath("$.errors.password").value("password is mandatory for registration"))
                .andExpect(jsonPath("$.errors.email").value("email is mandatory for registration"));
    }

    @Test
    void registerUserProfile_existingUsername_returnsBadRequestAndError() throws Exception {
        // given
        UserProfile existingUser = UserProfile.builder()
                .username("charlie")
                .build();
        userProfileRepository.save(existingUser);
        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .username("charlie")
                .password("password")
                .email("charlie@email.com")
                .build();
        String registrationRequestString = objectMapper.writeValueAsString(registrationRequest);

        // when
        mockMVC.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationRequestString))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("username "+ registrationRequest.getUsername() + " already exists"));
    }

    @Test
    void login_validRequest_returnsToken() throws Exception {
        // given
        LoginRequest loginRequest = LoginRequest.builder()
                .username("admin")
                .password("admin")
                .build();
        String loginRequestString = objectMapper.writeValueAsString(loginRequest);

        // when
        mockMVC.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequestString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("token").exists());
    }

    @Test
    void login_blankFields_returnsErrors() throws Exception {
        // given
        LoginRequest loginRequest = LoginRequest.builder()
                .username("")
                .password("")
                .build();
        String loginRequestString = objectMapper.writeValueAsString(loginRequest);

        // when
        mockMVC.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequestString))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").value("username is mandatory when logging in"))
                .andExpect(jsonPath("$.errors.password").value("password is mandatory when logging in"));
    }

    @Test
    void login_wrongPassword_returnsError() throws Exception {
        // given
        LoginRequest loginRequest = LoginRequest.builder()
                .username("admin")
                .password("wrongpw")
                .build();
        String loginRequestString = objectMapper.writeValueAsString(loginRequest);

        // when
        mockMVC.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequestString))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_userNotFound_returnsBadRequestAndError() throws Exception {
        // given
        LoginRequest loginRequest = LoginRequest.builder()
                .username("apple")
                .password("apple")
                .build();
        String loginRequestString = objectMapper.writeValueAsString(loginRequest);
        // when
        mockMVC.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequestString))
                .andExpect(status().isForbidden());
    }

    @Test
    void validateToken_validToken_returnsSuccess() throws Exception {
        // given
        UserProfile admin = userProfileRepository.findByUsername("admin").orElseThrow(()->null);
        String token = jwtUtils.generateToken(admin.getUsername(), admin.getId(), admin.getRoles().stream().map(Role::getName).toList());

        // when
        mockMVC.perform(MockMvcRequestBuilders.get("/auth/validateToken")
                .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(admin.getId()))
                .andExpect(jsonPath("$.username").value(admin.getUsername()))
                .andExpect(jsonPath("$.authorities", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.authorities", Matchers.containsInAnyOrder(admin.getRoles().get(0).getName())));
    }
}

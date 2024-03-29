package com.yilinglin10.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequest {
    @NotBlank(message="username is mandatory when logging in")
    private String username;
    @NotBlank(message="password is mandatory when logging in")
    private String password;
}

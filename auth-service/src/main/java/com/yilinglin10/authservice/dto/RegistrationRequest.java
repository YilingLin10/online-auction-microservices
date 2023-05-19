package com.yilinglin10.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistrationRequest {
    private Long id;
    @NotBlank(message="username is mandatory for registration")
    private String username;
    @NotBlank(message="password is mandatory for registration")
    private String password;
    @NotBlank(message="email is mandatory for registration")
    @Email(message="invalid email")
    private String email;
    @Pattern(regexp="((?=(09))[0-9]{10})$", message="invalid phone number")
    private String phoneNumber;
}

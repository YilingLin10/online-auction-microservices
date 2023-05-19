package com.yilinglin10.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditUserProfileRequest {
    @NotBlank(message = "please provide new username")
    private String username;
    @NotBlank(message = "please provide new password")
    private String password;
    @Email(message = "Please provide valid email")
    private String email;
    @Pattern(regexp = "((?=(09))[0-9]{10})$", message = "Please provide valid phone number")
    private String phoneNumber;
}

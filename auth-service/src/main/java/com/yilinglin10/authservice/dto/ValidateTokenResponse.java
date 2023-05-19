package com.yilinglin10.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenResponse {
    private Long userId;
    private String username;
    private List<String> authorities;
}

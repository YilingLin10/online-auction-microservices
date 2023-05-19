package com.yilinglin10.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenResponse {
    private Long userId;
    private String username;
    private List<String> authorities;
}

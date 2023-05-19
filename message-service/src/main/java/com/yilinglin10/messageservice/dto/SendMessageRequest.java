package com.yilinglin10.messageservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageRequest {
    @NotBlank(message="recipient is mandatory")
    private String recipientUsername;
    @NotBlank(message="title is mandatory")
    private String title;
    @NotBlank(message="content is mandatory")
    private String content;
}

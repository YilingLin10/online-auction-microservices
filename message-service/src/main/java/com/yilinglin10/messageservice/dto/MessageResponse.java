package com.yilinglin10.messageservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {
    private String id;
    private Long senderId;
    private Long recipientId;
    private String title;
    private String content;
    private LocalDateTime timestamp;
}

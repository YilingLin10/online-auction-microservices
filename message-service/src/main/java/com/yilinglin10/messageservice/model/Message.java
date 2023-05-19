package com.yilinglin10.messageservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("message")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {
    @Id
    private String id;
    private Long senderId;
    private Long recipientId;
    private String subject;
    private String content;
    private LocalDateTime timestamp;
}

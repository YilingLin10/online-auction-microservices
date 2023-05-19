package com.yilinglin10.messageservice;

import com.yilinglin10.messageservice.model.Message;
import com.yilinglin10.messageservice.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@Slf4j
@SpringBootApplication
public class MessageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessageServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadData(MessageRepository messageRepository) {
        return args -> {
            log.info("cleaning existing messages...");
            messageRepository.deleteAll();
            for (int i=0; i<10; i++) {
                Message message = Message.builder()
                        .senderId((long) 1)
                        .recipientId((long) 2)
                        .subject("test message "+ i)
                        .timestamp(LocalDateTime.now())
                        .build();
                messageRepository.insert(message);
            }
        };
    }
}

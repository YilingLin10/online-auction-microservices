package com.yilinglin10.messageservice.service;

import com.yilinglin10.messageservice.dto.MessageResponse;
import com.yilinglin10.messageservice.dto.SendMessageRequest;
import com.yilinglin10.messageservice.exception.InvalidUserException;
import com.yilinglin10.messageservice.exception.MessageNotFoundException;
import com.yilinglin10.messageservice.exception.RecipientNotFoundException;
import com.yilinglin10.messageservice.model.Message;
import com.yilinglin10.messageservice.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService{

    private final MessageRepository messageRepository;
    private final WebClient.Builder webClientBuilder;

    private static final long ADMIN_USER_ID = 1;

    public String sendMessage(SendMessageRequest request, Long userId) {
        String result = webClientBuilder.build().get()
                .uri("http://user-service/api/users", uriBuilder -> uriBuilder.queryParam("username", request.getRecipientUsername()).build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try {
            assert result != null;
            Long.parseLong(result);
        } catch (NumberFormatException e) {
            throw new RecipientNotFoundException(request.getRecipientUsername());
        }

        Message message = Message.builder()
                .senderId(userId)
                .recipientId(Long.parseLong(result))
                .subject(request.getTitle())
                .content(request.getContent())
                .timestamp(LocalDateTime.now())
                .build();
        messageRepository.insert(message);
        return "successful";
    }

    public MessageResponse getMessage(String id, Long userId) {
        Message message = messageRepository.findById(id).orElseThrow(()-> new MessageNotFoundException(id));
        boolean isValidUser = validateSender(message, userId) || validateRecipient(message, userId);
        if (!isValidUser) {
            throw new InvalidUserException(id);
        }
        return mapEntityToDto(message);
    }

    public List<MessageResponse> findBySenderId(Long senderId, Integer offset, Integer pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by("timestamp"));
        List<Message> messages = messageRepository.findBySenderId(senderId, pageable);
        return messages.stream().map(this::mapEntityToDto).toList();
    }

    public List<MessageResponse> findByRecipientId(Long recipientId, Integer offset, Integer pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by("timestamp"));
        List<Message> messages = messageRepository.findByRecipientId(recipientId, pageable);
        return messages.stream().map(this::mapEntityToDto).toList();
    }

    private boolean validateSender(Message message, Long userId) {
        return message.getSenderId().equals(userId);
    }

    private boolean validateRecipient(Message message, Long userId) {
        return message.getRecipientId().equals(userId);
    }

    private MessageResponse mapEntityToDto(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .recipientId(message.getRecipientId())
                .title(message.getSubject())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();
    }

    public void sendNotification(String subject, String content, LocalDateTime timestamp, List<Long> receivers) {
        for (Long receiver: receivers) {
            Message notification = Message.builder()
                            .senderId(ADMIN_USER_ID)
                            .recipientId(receiver)
                            .subject(subject)
                            .content(content)
                            .timestamp(timestamp)
                            .build();
            messageRepository.insert(notification);
        }
    }
}

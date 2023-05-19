package com.yilinglin10.messageservice.repository;

import com.yilinglin10.messageservice.model.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findBySenderId(Long senderId, Pageable pageable);

    List<Message> findByRecipientId(Long recipientId, Pageable pageable);
}

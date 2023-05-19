package com.yilinglin10.messageservice.controller;

import com.yilinglin10.messageservice.dto.MessageResponse;
import com.yilinglin10.messageservice.dto.SendMessageRequest;
import com.yilinglin10.messageservice.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getMessage(@PathVariable String id, @RequestHeader("loggedInUser") String userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", messageService.getMessage(id, Long.parseLong(userId)));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sender")
    public ResponseEntity<Object> getSent(@RequestParam("offset") Integer offset, @RequestParam("page-size") Integer pageSize, @RequestHeader("loggedInUser") String userId) {
        Map<String, Object> response = new HashMap<>();
        List<MessageResponse> messages = messageService.findBySenderId(Long.parseLong(userId), offset, pageSize);
        response.put("messages", messages);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recipient")
    public ResponseEntity<Object> getInbox(@RequestParam("offset") Integer offset, @RequestParam("page-size") Integer pageSize, @RequestHeader("loggedInUser") String userId) {
        Map<String, Object> response = new HashMap<>();
        List<MessageResponse> messages = messageService.findByRecipientId(Long.parseLong(userId), offset, pageSize);
        response.put("messages", messages);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Object> sendMessage(@Valid @RequestBody SendMessageRequest request, @RequestHeader("loggedInUser") String userId) {
        Map<String, Object> response = new HashMap<>();
        String result = messageService.sendMessage(request, Long.parseLong(userId));
        response.put("response", result);
        return ResponseEntity.status(result.equals("successful") ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }

}

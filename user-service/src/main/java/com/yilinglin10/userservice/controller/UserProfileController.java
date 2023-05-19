package com.yilinglin10.userservice.controller;

import com.yilinglin10.userservice.dto.EditUserProfileRequest;
import com.yilinglin10.userservice.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public String getIdByUsername(@RequestParam String username) {
        return userProfileService.getIdByUsername(username);
    }

    @GetMapping(value = "{id}")
    public ResponseEntity<Object> getUserProfile(@PathVariable("id") Long userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("user", userProfileService.getUserProfile(userId));
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "{id}")
    public ResponseEntity<Object> updateUserProfile(@PathVariable("id") Long userId, @Valid @RequestBody EditUserProfileRequest userProfileDto) {
        Map<String, Object> response = new HashMap<>();
        String result = userProfileService.updateUserProfile(userId, userProfileDto);
        if (result.equals("successful")) {
            response.put("response", result);
            return ResponseEntity.ok(response);
        }else {
            response.put("response", "failed to update user profile");
            return ResponseEntity.badRequest().body(response);
        }
    }

}

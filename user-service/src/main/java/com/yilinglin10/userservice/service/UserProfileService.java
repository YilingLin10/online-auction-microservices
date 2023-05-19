package com.yilinglin10.userservice.service;

import com.yilinglin10.userservice.dto.EditUserProfileRequest;
import com.yilinglin10.userservice.dto.UserProfileResponse;
import com.yilinglin10.userservice.entity.UserProfile;
import com.yilinglin10.userservice.exception.UserNotFoundException;
import com.yilinglin10.userservice.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserProfileResponse getUserProfile(Long userId) {
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("cannot find user with id " + userId));
        return mapEntityToDto(userProfile);
    }

    public String updateUserProfile(Long userId, EditUserProfileRequest editUserProfileRequest) {
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("cannot find user with id" + userId));
        userProfile.setUsername(editUserProfileRequest.getUsername());
        userProfile.setEmail(editUserProfileRequest.getEmail());
        userProfile.setPassword(passwordEncoder.encode(editUserProfileRequest.getPassword()));
        userProfile.setPhoneNumber(editUserProfileRequest.getPhoneNumber());
        userProfileRepository.save(userProfile);
        return "successful";
    }

    private UserProfileResponse mapEntityToDto(UserProfile userProfile) {
        return UserProfileResponse.builder()
                .id(userProfile.getId())
                .username(userProfile.getUsername())
                .phoneNumber(userProfile.getPhoneNumber())
                .email(userProfile.getEmail())
                .build();
    }

    public String getIdByUsername(String username) {
        if (!userProfileRepository.existsByUsername(username)) throw new UserNotFoundException("cannot find user with name "+ username);
        UserProfile userProfile = userProfileRepository.findByUsername(username);
        return String.valueOf(userProfile.getId());
    }
}

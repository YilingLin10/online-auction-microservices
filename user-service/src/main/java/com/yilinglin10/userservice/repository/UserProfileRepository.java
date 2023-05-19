package com.yilinglin10.userservice.repository;

import com.yilinglin10.userservice.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    UserProfile findByUsername(String username);

    boolean existsByUsername(String username);
}

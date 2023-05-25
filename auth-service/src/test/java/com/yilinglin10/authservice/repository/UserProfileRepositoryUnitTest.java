package com.yilinglin10.authservice.repository;

import com.yilinglin10.authservice.entity.Role;
import com.yilinglin10.authservice.entity.UserProfile;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
class UserProfileRepositoryUnitTest {

    @Autowired
    UserProfileRepository userProfileRepository;

    private UserProfile user;

    @BeforeEach
    void setUp() {
        Role newRole = Role.builder()
                .name("VISITOR")
                .build();
        user = UserProfile.builder()
                .username("apple")
                .password("password")
                .email("apple@gmail.com")
                .roles(List.of(newRole))
                .build();
    }

    @AfterEach
    void removeUser() {
        userProfileRepository.delete(user);
        user = null;
    }



    @Test
    @DisplayName("findByUsername_existingUser_returnsUser")
    void findByUsername_existingUser_returnsUser() {
        userProfileRepository.save(user);
        Optional<UserProfile> foundUser = userProfileRepository.findByUsername("apple");
        Assertions.assertTrue(foundUser.isPresent());
    }

    @Test
    @DisplayName("existsByUsername_exists_returnsTrue")
    void existsByUsername_exists_returnsTrue() {
        userProfileRepository.save(user);
        Boolean doesExist = userProfileRepository.existsByUsername("apple");
        Assertions.assertTrue(doesExist);
    }

    @Test
    @DisplayName("existsByUsername_doesNotExist_returnsFalse")
    void existsByUsername_doesNotExist_returnsFalse() {
        Boolean doesNotExist = userProfileRepository.existsByUsername("apple");
        Assertions.assertFalse(doesNotExist);
    }
}
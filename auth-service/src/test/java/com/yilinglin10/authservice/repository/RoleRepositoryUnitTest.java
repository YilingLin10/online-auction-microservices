package com.yilinglin10.authservice.repository;

import com.yilinglin10.authservice.entity.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
public class RoleRepositoryUnitTest {

    @Autowired
    RoleRepository roleRepository;

    @Test
    @DisplayName("findByName_existingRole_returnsRole")
    void findByName() {
        Role newRole = Role.builder()
                .name("VISITOR")
                .build();
        roleRepository.save(newRole);

        Optional<Role> foundRole = roleRepository.findByName("VISITOR");
        Assertions.assertTrue(foundRole.isPresent());
    }
}
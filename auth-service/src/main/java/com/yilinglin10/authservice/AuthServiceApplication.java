package com.yilinglin10.authservice;

import com.yilinglin10.authservice.entity.Role;
import com.yilinglin10.authservice.entity.UserProfile;
import com.yilinglin10.authservice.repository.RoleRepository;
import com.yilinglin10.authservice.repository.UserProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@Slf4j
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }


    @Bean
    public CommandLineRunner loadData(PasswordEncoder passwordEncoder, UserProfileRepository userProfileRepository, RoleRepository roleRepository) {
        return args -> {
            userProfileRepository.deleteAll();
            roleRepository.deleteAll();
            Role admin = Role.builder().name("ADMIN").build();
            Role user = Role.builder().name("USER").build();

            UserProfile adminUser = UserProfile.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .email("admin@auction.com")
                    .roles(List.of(admin))
                    .build();
            UserProfile testUser = UserProfile.builder()
                    .username("test")
                    .password(passwordEncoder.encode("test"))
                    .email("test@auction.com")
                    .roles(List.of(user))
                    .build();
            log.info("adding admin user...");
            userProfileRepository.save(adminUser);

            log.info("adding test user...");
            userProfileRepository.save(testUser);

        };
    }
}

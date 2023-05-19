package com.yilinglin10.reviewservice;

import com.yilinglin10.reviewservice.repository.OrderRepository;
import com.yilinglin10.reviewservice.repository.ReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class ReviewServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner cleanDatabase(ReviewRepository reviewRepository, OrderRepository orderRepository) {
        return args-> {
            log.info("cleaning existing reviews and orders from database....");
            reviewRepository.deleteAll();
            orderRepository.deleteAll();
        };
    }
}

package com.yilinglin10.orderservice;

import com.yilinglin10.orderservice.model.Order;
import com.yilinglin10.orderservice.model.OrderStatus;
import com.yilinglin10.orderservice.repository.OrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableScheduling
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadData(OrderRepository orderRepository) {
        return args -> {
        };
    }
}

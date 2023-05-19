package com.yilinglin10.orderservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_orders")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long auctionId;
    // for notification
    private String auctionName;
    private Long sellerId;
    private Long buyerId;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime paymentDeadline;
    private LocalDateTime shippingDeadline;
}

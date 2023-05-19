package com.yilinglin10.orderservice.dto;

import com.yilinglin10.orderservice.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private Long auctionId;
    private String auctionName;
    private Long sellerId;
    private Long buyerId;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime paymentDeadline;
    private LocalDateTime shippingDeadline;
}

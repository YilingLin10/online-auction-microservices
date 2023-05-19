package com.yilinglin10.reviewservice.event.consume;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderFulfilledOrShippingDueEvent {
    private UUID id;
    private Long orderId;
    private Long sellerId;
    private Long buyerId;
}

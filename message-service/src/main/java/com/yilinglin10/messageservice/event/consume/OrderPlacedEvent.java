package com.yilinglin10.messageservice.event.consume;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPlacedEvent {
    private UUID id;
    private String auctionName;
    private Long sellerId;
    private Long buyerId;
    private LocalDateTime timestamp;
}

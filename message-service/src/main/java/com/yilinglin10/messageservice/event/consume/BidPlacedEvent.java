package com.yilinglin10.messageservice.event.consume;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BidPlacedEvent {
    private UUID id;
    private String auctionName;
    private Long sellerId;
    private List<Long> previousBidders;
    private Double currentPrice;
    private LocalDateTime timestamp;
}

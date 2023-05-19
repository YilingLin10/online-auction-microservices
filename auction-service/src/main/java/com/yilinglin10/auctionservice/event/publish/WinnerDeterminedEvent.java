package com.yilinglin10.auctionservice.event.publish;

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
public class WinnerDeterminedEvent {
    private UUID id;
    // for order placement
    private Long auctionId;
    // for notification
    private String auctionName;
    private Long sellerId;
    private Long winnerId;
    private List<Long> previousBidders;
    private LocalDateTime timestamp;
}

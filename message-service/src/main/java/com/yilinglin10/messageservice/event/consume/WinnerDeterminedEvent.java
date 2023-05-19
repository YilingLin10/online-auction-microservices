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
public class WinnerDeterminedEvent {
    private UUID id;
    private Long auctionId;
    private String auctionName;
    private Long sellerId;
    private Long winnerId;
    private List<Long> previousBidders;
    private LocalDateTime timestamp;
}

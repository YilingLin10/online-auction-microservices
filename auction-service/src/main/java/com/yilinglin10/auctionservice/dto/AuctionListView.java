package com.yilinglin10.auctionservice.dto;

import com.yilinglin10.auctionservice.model.AuctionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuctionListView {
    private Long id;
    private Long sellerId;
    private String name;
    private Double currentPrice;
    private LocalDateTime endAt;
    private AuctionStatus status;
}

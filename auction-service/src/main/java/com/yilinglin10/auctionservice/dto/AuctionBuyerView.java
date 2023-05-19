package com.yilinglin10.auctionservice.dto;

import com.yilinglin10.auctionservice.model.AuctionStatus;
import com.yilinglin10.auctionservice.model.Bid;
import com.yilinglin10.auctionservice.model.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuctionBuyerView {
    private Long id;
    private Long sellerId;
    private Item item;
    private Double currentPrice;
    private Long lastBidderId;
    private LocalDateTime createdAt;
    private LocalDateTime endAt;
    private AuctionStatus status;
    private List<Bid> bids;
}

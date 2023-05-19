package com.yilinglin10.auctionservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "t_auctions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long sellerId;
    @Embedded
    private Item item;
    private Double startPrice;
    private Double reservePrice;
    private Double currentPrice;
    private Long lastBidderId;
    private LocalDateTime createdAt;
    private LocalDateTime endAt;
    private AuctionStatus status;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Bid> bids;
}

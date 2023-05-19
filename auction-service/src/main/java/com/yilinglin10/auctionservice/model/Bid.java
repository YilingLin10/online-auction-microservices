package com.yilinglin10.auctionservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="t_bids")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long bidderId;
    private Double price;
    private LocalDateTime createdAt;
}

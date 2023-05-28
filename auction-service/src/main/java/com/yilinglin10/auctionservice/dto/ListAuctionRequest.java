package com.yilinglin10.auctionservice.dto;

import jakarta.validation.Constraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListAuctionRequest {
    @NotBlank
    private String name;
    private String description;
    @Positive
    private Double startPrice;
    @Positive
    private Double reservePrice;
    @NotNull
    private LocalDateTime endAt;
}

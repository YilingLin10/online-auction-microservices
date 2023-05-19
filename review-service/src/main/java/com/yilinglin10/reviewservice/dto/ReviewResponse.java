package com.yilinglin10.reviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewResponse {
    private String id;
    private Long orderId;
    private Long reviewerId;
    private Long sellerId;
    private Integer rating;
    private String comment;
}

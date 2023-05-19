package com.yilinglin10.reviewservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitReviewRequest {

    @NotNull(message="sellerId is mandatory")
    private Long sellerId;

    @NotNull(message = "orderId is mandatory")
    private Long orderId;

    @NotNull(message = "rating is mandatory")
    @Range(min=0, max=5, message = "rating must be within 0~5")
    private Integer rating;

    private String comment;
}

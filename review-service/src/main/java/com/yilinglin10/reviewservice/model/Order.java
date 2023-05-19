package com.yilinglin10.reviewservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "order")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    private String id;
    private Long orderId;
    private Long sellerId;
    private Long buyerId;
    private boolean isSellerReviewed;
}

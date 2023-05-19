package com.yilinglin10.reviewservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "review")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Review {
    @Id
    private String id;
    private Long orderId;
    private Long sellerId;
    private Long buyerId;
    private Integer rating;
    private String comment;
}

package com.yilinglin10.auctionservice.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    private String name;
    private String description;
}

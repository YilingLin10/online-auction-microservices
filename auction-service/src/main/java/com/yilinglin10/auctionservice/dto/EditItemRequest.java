package com.yilinglin10.auctionservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditItemRequest {
    @NotBlank(message = "Auction name is mandatory")
    private String name;
    private String description;
}

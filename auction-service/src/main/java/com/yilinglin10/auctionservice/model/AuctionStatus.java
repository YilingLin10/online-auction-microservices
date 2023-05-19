package com.yilinglin10.auctionservice.model;

import lombok.Getter;

@Getter
public enum AuctionStatus {
    ACTIVE("active"), SOLD("sold"), FAILED("failed");

    private final String code;

    AuctionStatus(String code) {
        this.code = code;
    }
}

package com.yilinglin10.auctionservice.exception;

public class InvalidSellerException extends RuntimeException {
    public InvalidSellerException(String userId) {
        super("Invalid user " + userId + " attempts to edit/delete an auction");
    }
}

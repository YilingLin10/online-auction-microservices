package com.yilinglin10.auctionservice.exception;

public class CannotDeleteAuctionException extends RuntimeException {
    public CannotDeleteAuctionException(Long id) {
        super("auction with id " + id + " has been bid on");
    }
}

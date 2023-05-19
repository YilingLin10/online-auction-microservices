package com.yilinglin10.auctionservice.exception;

public class InvalidPlaceBidRequestException extends RuntimeException{
    public InvalidPlaceBidRequestException(String message) {
        super(message);
    }
}

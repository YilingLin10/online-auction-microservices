package com.yilinglin10.reviewservice.exception;

public class InvalidReviewRequestException extends RuntimeException {
    public InvalidReviewRequestException() {
        super("user not allowed to review this order");
    }
}

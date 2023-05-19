package com.yilinglin10.orderservice.exception;

public class OrderDeadlineDueException extends RuntimeException {
    public OrderDeadlineDueException(Long orderId, String status) {
        super("order with id " + orderId + " is in status " + status);
    }
}

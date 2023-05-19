package com.yilinglin10.orderservice.model;

import jakarta.persistence.Converter;
import lombok.Getter;

@Getter
public enum OrderStatus {
    AWAITING_PAYMENT("awaiting_payment"), AWAITING_SHIPMENT("awaiting_shipment"), FULFILLED("fulfilled"), PAYMENT_DUE("payment_due"), SHIPPING_DUE("shipping_due");

    private final String code;
    private static final OrderStatus[] VALUES = values();

    OrderStatus(String code) {
        this.code = code;
    }

    public OrderStatus next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }
}

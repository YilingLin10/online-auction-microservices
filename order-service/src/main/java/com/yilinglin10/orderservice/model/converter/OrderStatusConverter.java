package com.yilinglin10.orderservice.model.converter;

import com.yilinglin10.orderservice.model.OrderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {
    @Override
    public String convertToDatabaseColumn(OrderStatus status) {
        return status.getCode();
    }

    @Override
    public OrderStatus convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }

        return Stream.of(OrderStatus.values())
                .filter(c -> c.getCode().equals(code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}

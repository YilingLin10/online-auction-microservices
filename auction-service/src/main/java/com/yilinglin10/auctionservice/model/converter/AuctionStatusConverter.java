package com.yilinglin10.auctionservice.model.converter;

import com.yilinglin10.auctionservice.model.AuctionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class AuctionStatusConverter implements AttributeConverter<AuctionStatus, String> {

    @Override
    public String convertToDatabaseColumn(AuctionStatus status) {
        return status.getCode();
    }

    @Override
    public AuctionStatus convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }

        return Stream.of(AuctionStatus.values())
                .filter(c -> c.getCode().equals(code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}

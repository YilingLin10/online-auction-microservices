package com.yilinglin10.orderservice.kafka;

import com.yilinglin10.orderservice.event.consume.WinnerDeterminedEvent;
import com.yilinglin10.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(id="order-placement-consumer", topics = "orderPlacementTopic")
public class OrderPlacementTopicListener {

    private final OrderService orderService;

    @KafkaHandler
    public void handleWinnerDeterminedEvent(WinnerDeterminedEvent event) {
        log.info("PLACING ORDER FOR AUCTION {}", event.getAuctionName());
        orderService.placeAnOrder(event.getAuctionId(), event.getAuctionName(), event.getSellerId(), event.getWinnerId(), event.getTimestamp());
    }
}

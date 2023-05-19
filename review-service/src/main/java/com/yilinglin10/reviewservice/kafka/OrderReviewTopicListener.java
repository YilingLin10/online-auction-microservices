package com.yilinglin10.reviewservice.kafka;

import com.yilinglin10.reviewservice.event.consume.OrderFulfilledOrShippingDueEvent;
import com.yilinglin10.reviewservice.model.Order;
import com.yilinglin10.reviewservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@KafkaListener(id = "order-review-consumer",topics="orderReviewTopic")
@RequiredArgsConstructor
@Component
public class OrderReviewTopicListener {

    private final OrderRepository orderRepository;

    @KafkaHandler
    public void handleOrderFulfilledOrShipmentDueEvent(OrderFulfilledOrShippingDueEvent event) {
        log.info("Handling OrderFulfilledOrShipmentDueEvent....");
        Order order = Order.builder()
                .orderId(event.getOrderId())
                .sellerId(event.getSellerId())
                .buyerId(event.getBuyerId())
                .isSellerReviewed(false)
                .build();
        orderRepository.save(order);
    }
}

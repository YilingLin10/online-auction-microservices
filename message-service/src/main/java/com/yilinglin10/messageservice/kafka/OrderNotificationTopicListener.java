package com.yilinglin10.messageservice.kafka;

import com.yilinglin10.messageservice.event.consume.OrderPlacedEvent;
import com.yilinglin10.messageservice.event.consume.OrderStatusUpdatedEvent;
import com.yilinglin10.messageservice.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@KafkaListener(id="order", topics = "orderNotificationTopic")
@Slf4j
public class OrderNotificationTopicListener {

    @Autowired
    private MessageService messageService;

    @KafkaHandler
    public void handleOrderPlacedEvent(OrderPlacedEvent orderPlacedEvent) {
        log.info("ORDER FOR AUCTION {} PLACED: notifying seller and buyer", orderPlacedEvent.getAuctionName());
        String seller_subject = "You Have A New Order for "+orderPlacedEvent.getAuctionName();
        String seller_content = "You have a new order for "+orderPlacedEvent.getAuctionName()+". The buyer is supposed to transfer the payment before deadline. Please confirm after receiving the payment.";
        messageService.sendNotification(seller_subject, seller_content, orderPlacedEvent.getTimestamp(), List.of(orderPlacedEvent.getSellerId()));

        String buyer_subject = "Your Order for "+orderPlacedEvent.getAuctionName()+" Is Placed";
        String buyer_content = "Your order for "+orderPlacedEvent.getAuctionName()+" is placed. Please transfer the payment before deadline.";
        messageService.sendNotification(buyer_subject, buyer_content, orderPlacedEvent.getTimestamp(), List.of(orderPlacedEvent.getBuyerId()));
    }

    @KafkaHandler
    public void handleOrderStatusUpdatedEvent(OrderStatusUpdatedEvent orderStatusUpdatedEvent) {
        log.info("ORDER FOR AUCTION {} STATUS UPDATED TO {}: notifying seller and buyer...", orderStatusUpdatedEvent.getAuctionName(), orderStatusUpdatedEvent.getStatus());
        String subject = "Status Updated on Your Order for "+orderStatusUpdatedEvent.getAuctionName();
        String content = "The status of your order "+ orderStatusUpdatedEvent.getAuctionName() + " is updated to "+ orderStatusUpdatedEvent.getStatus();
        messageService.sendNotification(subject, content, orderStatusUpdatedEvent.getTimestamp(), List.of(orderStatusUpdatedEvent.getSellerId(), orderStatusUpdatedEvent.getBuyerId()));
    }
}

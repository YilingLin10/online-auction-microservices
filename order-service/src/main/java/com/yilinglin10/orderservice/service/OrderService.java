package com.yilinglin10.orderservice.service;

import com.yilinglin10.orderservice.dto.OrderResponse;
import com.yilinglin10.orderservice.event.publish.OrderFulfilledOrShippingDueEvent;
import com.yilinglin10.orderservice.event.publish.OrderPlacedEvent;
import com.yilinglin10.orderservice.event.publish.OrderStatusUpdatedEvent;
import com.yilinglin10.orderservice.exception.InvalidUserException;
import com.yilinglin10.orderservice.exception.OrderDeadlineDueException;
import com.yilinglin10.orderservice.exception.OrderNotFoundException;
import com.yilinglin10.orderservice.model.Order;
import com.yilinglin10.orderservice.model.OrderStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.yilinglin10.orderservice.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private static final int PAYMENT_DEADLINE_IN_DAYS = 5;
    private static final int SHIPPING_DEADLINE_IN_DAYS = 5;
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // kafkaHandler for OrderPlacementEvent
    public void placeAnOrder(Long auctionId, String auctionName, Long sellerId, Long buyerId, LocalDateTime timestamp) {
        Order order = Order.builder()
                .auctionId(auctionId)
                .auctionName(auctionName)
                .sellerId(sellerId)
                .buyerId(buyerId)
                .createdAt(timestamp)
                .status(OrderStatus.AWAITING_PAYMENT)
                .paymentDeadline(timestamp.plusDays(PAYMENT_DEADLINE_IN_DAYS))
                .shippingDeadline(timestamp.plusDays(SHIPPING_DEADLINE_IN_DAYS))
                .build();
        orderRepository.save(order);

        log.info("Order for auction {} is placed, publishing OrderPlacedEvent to orderNotificationTopic...", auctionName);
        kafkaTemplate.send("orderNotificationTopic", OrderPlacedEvent.builder()
                .id(UUID.randomUUID())
                .auctionName(auctionName)
                .sellerId(sellerId)
                .buyerId(buyerId)
                .timestamp(LocalDateTime.now())
                .build());
    }

    public OrderResponse getOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId).orElseThrow(()-> new OrderNotFoundException(orderId));
        boolean isValidUser = validateSeller(order, userId) || validateBuyer(order, userId);
        if (!isValidUser) {
            throw new InvalidUserException(orderId);
        }
        return mapEntityToDto(order);
    }

    public String updateOrderStatus(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId).orElseThrow(()-> new OrderNotFoundException(orderId));
        boolean isValidSeller = validateSeller(order, userId);
        boolean isValidBuyer = validateBuyer(order, userId);
        if (!isValidSeller && !isValidBuyer) {
            throw new InvalidUserException(orderId);
        }
        if (order.getStatus().equals(OrderStatus.PAYMENT_DUE) || order.getStatus().equals(OrderStatus.SHIPPING_DUE)) {
            throw new OrderDeadlineDueException(orderId, order.getStatus().getCode());
        }
        if ((isValidSeller && !order.getStatus().equals(OrderStatus.AWAITING_PAYMENT)) || (isValidBuyer && !order.getStatus().equals(OrderStatus.AWAITING_SHIPMENT)) ) {
            return "cannot update order status";
        }
        order.setStatus(order.getStatus().next());
        orderRepository.save(order);

        log.info("ORDER FOR AUCTION {} STATUS UPDATED, publishing OrderStatusUpdatedEvent to orderNotificationTopic", order.getAuctionName());
        kafkaTemplate.send("orderNotificationTopic", OrderStatusUpdatedEvent.builder()
                        .id(UUID.randomUUID())
                        .auctionName(order.getAuctionName())
                        .sellerId(order.getSellerId())
                        .buyerId(order.getBuyerId())
                        .status(order.getStatus().getCode())
                        .timestamp(LocalDateTime.now())
                        .build());

        log.info("ORDER FOR AUCTION {} IS FULFILLED, publishing OrderFulfilledOrShippingDueEvent to orderReviewTopic", order.getAuctionName());
        if (order.getStatus().equals(OrderStatus.FULFILLED)) {
            kafkaTemplate.send("orderReviewTopic", OrderFulfilledOrShippingDueEvent.builder()
                    .orderId(orderId)
                    .sellerId(order.getSellerId())
                    .buyerId(order.getBuyerId())
                    .build());
        }
        return "successful";
    }

    public List<OrderResponse> findByUserId(Long userId, Integer offset, Integer pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by("deadline"));
        List<Order> orders = orderRepository.findBySellerIdOrBuyerId(userId, userId, pageable);
        return orders.stream().map(this::mapEntityToDto).toList();
    }

    public List<OrderResponse> findByUserIdAndStatusIn(List<String> statusList, Long userId, Integer offset, Integer pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by("createdAt"));
        List<OrderStatus> orderStatusList = statusList.stream().map(this::convertToEnumStatus).toList();
        List<Order> orders = orderRepository.findBySellerIdOrBuyerIdAndStatusIn(userId, userId, orderStatusList, pageable);
        return orders.stream().map(this::mapEntityToDto).toList();
    }

    private OrderStatus convertToEnumStatus(String code) {
        if (code == null) {
            return null;
        }

        return Stream.of(OrderStatus.values())
                .filter(c -> c.getCode().equals(code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    private boolean validateBuyer(Order order, Long userId) {
        return order.getBuyerId().equals(userId);
    }

    private boolean validateSeller(Order order, Long userId) {
        return order.getSellerId().equals(userId);
    }

    private OrderResponse mapEntityToDto(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .auctionId(order.getAuctionId())
                .auctionName(order.getAuctionName())
                .sellerId(order.getSellerId())
                .buyerId(order.getBuyerId())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .paymentDeadline(order.getPaymentDeadline())
                .shippingDeadline(order.getShippingDeadline())
                .build();
    }

    // scheduled tasks
    public void checkOrderDeadline(LocalDateTime currTime) {
        for (Order order: orderRepository.findDueOrders(currTime, OrderStatus.AWAITING_PAYMENT, OrderStatus.AWAITING_SHIPMENT)) {
            updateOrderStatusToDue(order);

            log.info("order-{} failed, publish OrderStatusUpdatedEvent to orderNotificationTopic...", order.getId());
            kafkaTemplate.send("orderNotificationTopic", OrderStatusUpdatedEvent.builder()
                    .id(UUID.randomUUID())
                    .auctionName(order.getAuctionName())
                    .sellerId(order.getSellerId())
                    .buyerId(order.getBuyerId())
                    .status(order.getStatus().getCode())
                    .timestamp(currTime)
                    .build());
        }
    }

    private void updateOrderStatusToDue(Order order) {
        if (order.getStatus().equals(OrderStatus.AWAITING_PAYMENT)) {
            order.setStatus(OrderStatus.PAYMENT_DUE);
            orderRepository.save(order);
        }else {
            order.setStatus(OrderStatus.SHIPPING_DUE);
            orderRepository.save(order);

            log.info("ORDER FOR AUCTION {}: SHIPMENT_DUE, publishing OrderFulfilledOrShippingDueEvent to orderReviewTopic", order.getAuctionName());
            kafkaTemplate.send("orderReviewTopic", OrderFulfilledOrShippingDueEvent.builder()
                    .orderId(order.getId())
                    .sellerId(order.getSellerId())
                    .buyerId(order.getBuyerId())
                    .build());
        }
    }
}

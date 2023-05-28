package com.yilinglin10.orderservice.repository;

import com.yilinglin10.orderservice.model.Order;
import com.yilinglin10.orderservice.model.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findBySellerIdOrBuyerId(Long sellerId, Long buyerId, Pageable pageable);
    List<Order> findBySellerIdOrBuyerIdAndStatus(Long sellerId, Long buyerId, OrderStatus status, Pageable pageable);

    @Query(
            "SELECT o FROM Order o " +
            "WHERE (o.status IN :statusList) " +
            "AND (o.sellerId = :userId OR o.buyerId = :userId)"
    )
    List<Order> findByUserIdAndStatusIn(@Param("userId") Long userId, @Param("statusList") List<OrderStatus> orderStatusList, Pageable pageable);

    @Query(
            "SELECT o FROM Order o " +
            "WHERE (o.paymentDeadline <= :currTime AND o.status = :awaitingPayment) " +
            "OR (o.shippingDeadline <= :currTime AND o.status = :awaitingShipping)"
    )
    List<Order> findDueOrders(@Param("currTime") LocalDateTime currTime, @Param("awaitingPayment") OrderStatus awaitingPayment, @Param("awaitingShipping") OrderStatus awaitingShipping);

    Optional<Object> findByAuctionId(Long auctionId);
}

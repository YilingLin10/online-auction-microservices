package com.yilinglin10.reviewservice.repository;

import com.yilinglin10.reviewservice.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
    Order findByOrderId(Long orderId);
}

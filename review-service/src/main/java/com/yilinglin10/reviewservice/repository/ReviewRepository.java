package com.yilinglin10.reviewservice.repository;

import com.yilinglin10.reviewservice.model.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findBySellerId(Long sellerId, Pageable pageable);
    List<Review> findBySellerId(Long sellerId);
}

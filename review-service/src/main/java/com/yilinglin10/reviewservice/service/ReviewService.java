package com.yilinglin10.reviewservice.service;

import com.yilinglin10.reviewservice.dto.ReviewResponse;
import com.yilinglin10.reviewservice.dto.SubmitReviewRequest;
import com.yilinglin10.reviewservice.exception.InvalidReviewRequestException;
import com.yilinglin10.reviewservice.exception.ReviewNotFoundException;
import com.yilinglin10.reviewservice.model.Order;
import com.yilinglin10.reviewservice.model.Review;
import com.yilinglin10.reviewservice.repository.OrderRepository;
import com.yilinglin10.reviewservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String,Object> kafkaTemplate;
    private final WebClient.Builder webClientBuilder;

    public String submitReview(SubmitReviewRequest request, Long userId) {
        if (!canReviewOrder(request.getOrderId(), request.getSellerId(), userId)) {
            throw new InvalidReviewRequestException();
        }
        Review review = Review.builder()
                .orderId(request.getOrderId())
                .sellerId(request.getSellerId())
                .buyerId(userId)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        reviewRepository.insert(review);

        return "successful";
    }

    private boolean canReviewOrder(Long orderId, Long sellerId, Long buyerId) {
        Order order = orderRepository.findByOrderId(orderId);
        if (order == null) return false;
        return order.getSellerId().equals(sellerId) && order.getBuyerId().equals(buyerId);
    }

    public List<ReviewResponse> findBySellerId(Long sellerId, int offset, int pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize);
        List<Review> reviews = reviewRepository.findBySellerId(sellerId, pageable);
        return reviews.stream().map(this::mapEntityToDto).toList();
    }

    public Double getAverageRating(Long sellerId) {
        List<Review> reviews = reviewRepository.findBySellerId(sellerId);
        return calculateAverageRating(reviews);
    }

    private ReviewResponse mapEntityToDto(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrderId())
                .sellerId(review.getSellerId())
                .reviewerId(review.getBuyerId())
                .rating(review.getRating())
                .comment(review.getComment())
                .build();
    }

    private Double calculateAverageRating(List<Review> reviews) {
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream()
                .map(Review::getRating)
                .mapToDouble(rating -> rating)
                .average()
                .orElse(0.0);
    }

}

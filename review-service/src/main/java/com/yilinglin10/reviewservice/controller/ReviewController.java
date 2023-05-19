package com.yilinglin10.reviewservice.controller;

import com.yilinglin10.reviewservice.dto.SubmitReviewRequest;
import com.yilinglin10.reviewservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Object> submitReview(@Valid @RequestBody SubmitReviewRequest request, @RequestHeader("loggedInUser") String userId) {
        Map<String, Object> response = new HashMap<>();
        String result = reviewService.submitReview(request, Long.parseLong(userId));
        response.put("response", result);
        return ResponseEntity.status(result.equals("successful") ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }

    @GetMapping
    public ResponseEntity<Object> findBySellerId(@RequestParam("seller") Long sellerId, @RequestParam("offset") Integer offset, @RequestParam("page-size") Integer pageSize) {
        Map<String, Object> response = new HashMap<>();
        response.put("reviews", reviewService.findBySellerId(sellerId, offset, pageSize));
        return ResponseEntity.ok(response);
    }

    @GetMapping(value= "/average-rating/{sellerId}")
    public ResponseEntity<Object> getAverageRating(@PathVariable Long sellerId) {
        Map<String, Object> response = new HashMap<>();
        response.put("average-rating", reviewService.getAverageRating(sellerId));
        return ResponseEntity.ok(response);
    }
}

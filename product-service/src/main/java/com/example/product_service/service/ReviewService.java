package com.example.product_service.service;

import com.example.product_service.dto.request.ReviewRequestDto;
import com.example.product_service.dto.response.ReviewResponseDto;

public interface ReviewService {
    ReviewResponseDto saveReview(Long productId, Long userId, ReviewRequestDto requestDto);
}

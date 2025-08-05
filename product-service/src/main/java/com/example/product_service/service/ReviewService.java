package com.example.product_service.service;

import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ReviewResponseDto;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    PageDto<ReviewResponseDto> getReviewList(Long productId, Pageable pageable);
}

package com.example.product_service.service;

import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.repository.ReviewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService{

    private final ReviewsRepository reviewsRepository;

    @Override
    public PageDto<ReviewResponse> getReviewList(Long productId, Pageable pageable) {
//        Page<Reviews> result = reviewsRepository.findAllByProductId(productId, pageable);
//
//        List<ReviewResponseDto> content = result.getContent().stream().map(ReviewResponseDto::new).toList();
//        return new PageDto<>(
//                content,
//                pageable.getPageNumber(),
//                result.getTotalPages(),
//                pageable.getPageSize(),
//                result.getTotalElements()
//
//        );
        return null;
    }
}

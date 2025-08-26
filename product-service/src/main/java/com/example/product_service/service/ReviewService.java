package com.example.product_service.service;

import com.example.product_service.repository.ReviewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewsRepository reviewsRepository;


    public void deleteReviewById(Long reviewId, Long userId) {

    }
}

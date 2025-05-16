package com.example.product_service.service;

import com.example.product_service.dto.request.ReviewRequestDto;
import com.example.product_service.dto.response.ReviewResponseDto;
import com.example.product_service.entity.Products;
import com.example.product_service.entity.ReviewImages;
import com.example.product_service.entity.Reviews;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.repository.ReviewsRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.URL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService{

    private final ReviewsRepository reviewsRepository;
    private final ProductsRepository productsRepository;

    @Transactional
    @Override
    public ReviewResponseDto saveReview(Long productId, Long userId, ReviewRequestDto requestDto) {
        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Not Found Product"));

        Reviews review = new Reviews(product, userId, requestDto.getRating(), requestDto.getContent());

        requestDto.getImgUrls().stream()
                .map(ReviewImages::new)
                .forEach(review::addImage);

        Reviews saved = reviewsRepository.save(review);
        return new ReviewResponseDto(saved);
    }
}

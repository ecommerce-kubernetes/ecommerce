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
        /*TODO 리뷰는 해당 상품을 주문한 회원만 추가 가능해야 하므로 주문서비스에 해당
            1. 상품서비스에서 회원 ID , 주문 ID , 상품 ID , 리뷰 정보를 추가해 요청을 보냄
            2. 상품서비스에서 주문 ID로 주문 서비스에 해당 주문 정보를 받아옴
            3. 리뷰 요청을 보낸 회원이 해당 주문을 한 회원인지?, 주문상태가 리뷰작성이 가능한 상태인지?,
               해당 주문에 리뷰 요청을 보낸 상품 ID가 있는지 확인해 리뷰를 등록
        */
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

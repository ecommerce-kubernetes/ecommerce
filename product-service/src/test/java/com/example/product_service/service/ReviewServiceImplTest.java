package com.example.product_service.service;

import com.example.product_service.dto.request.ReviewRequestDto;
import com.example.product_service.dto.response.ReviewResponseDto;
import com.example.product_service.entity.Products;
import com.example.product_service.entity.Reviews;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.repository.ReviewsRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
public class ReviewServiceImplTest {

    @Autowired
    ReviewService reviewService;
    @Autowired
    ReviewsRepository reviewsRepository;
    @Autowired
    ProductsRepository productsRepository;

    private Products apple;

//    @BeforeEach
//    void initProductData(){
//        apple = productsRepository.save(new Products("청송사과", "청송사과 3EA", 3000, 10, null));
//    }

//    @Test
//    @Transactional
//    @DisplayName("리뷰 등록 구현")
//    void saveReviewTest(){
//        Long productId = apple.getId();
//        Long userId = 1L;
//
//        ReviewRequestDto reviewRequestDto =
//                new ReviewRequestDto(
//                        1L,
//                        4,
//                        "맛있습니다",
//                        List.of("http://reviewImg1.jpg", "http://reviewImg2.jpg")
//                );
//
//        ReviewResponseDto reviewResponseDto =
//                reviewService.saveReview(productId, userId, reviewRequestDto);
//
//        assertThat(reviewResponseDto.getRating()).isEqualTo(reviewRequestDto.getRating());
//        assertThat(reviewResponseDto.getUserId()).isEqualTo(userId);
//        assertThat(reviewResponseDto.getContent()).isEqualTo(reviewRequestDto.getContent());
//        assertThat(reviewResponseDto.getProductId()).isEqualTo(productId);
//    }

}

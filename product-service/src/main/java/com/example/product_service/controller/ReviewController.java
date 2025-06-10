package com.example.product_service.controller;

import com.example.product_service.controller.util.SortFieldValidator;
import com.example.product_service.dto.request.ReviewRequestDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ReviewResponseDto;
import com.example.product_service.entity.Reviews;
import com.example.product_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final SortFieldValidator sortFieldValidator;

    @PostMapping("/{productId}")
    public ResponseEntity<ReviewResponseDto> registerReview(@PathVariable("productId") Long productId,
                                                            @RequestHeader("X-User-Id") Long userId,
                                                            @RequestBody @Validated ReviewRequestDto reviewRequestDto){
//        ReviewResponseDto reviewResponseDto = reviewService.saveReview(productId, userId, reviewRequestDto);
//        return ResponseEntity.status(HttpStatus.CREATED).body(reviewResponseDto);
        return null;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<PageDto<ReviewResponseDto>> getAllReviewsByProductId(@PathVariable("productId") Long productId,
                                                                        @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
        sortFieldValidator.validateSortFields(pageable.getSort(), Reviews.class, null);
        PageDto<ReviewResponseDto> result = reviewService.getReviewList(productId, pageable);
        return ResponseEntity.ok(result);
    }
}

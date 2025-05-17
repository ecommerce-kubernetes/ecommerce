package com.example.product_service.controller;

import com.example.product_service.dto.request.ReviewRequestDto;
import com.example.product_service.dto.response.ReviewResponseDto;
import com.example.product_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{productId}")
    public ResponseEntity<ReviewResponseDto> registerReview(@PathVariable("productId") Long productId,
                                                            @RequestHeader("X-User-Id") Long userId,
                                                            @RequestBody @Validated ReviewRequestDto reviewRequestDto){
        ReviewResponseDto reviewResponseDto = reviewService.saveReview(productId, userId, reviewRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewResponseDto);
    }
}

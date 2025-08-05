package com.example.product_service.controller;

import com.example.product_service.controller.util.specification.annotation.BadRequestApiResponse;
import com.example.product_service.controller.util.specification.annotation.ForbiddenApiResponse;
import com.example.product_service.controller.util.specification.annotation.NotFoundApiResponse;
import com.example.product_service.dto.request.review.ReviewRequest;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ReviewResponseDto;
import com.example.product_service.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Review", description = "리뷰 관련 API")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 등록")
    @ApiResponse(responseCode = "201", description = "등록 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PostMapping("/products/{productId}/variants/{variantId}/reviews")
    public ResponseEntity<ReviewResponseDto> createReview(@PathVariable("productId") Long productId,
                                                          @PathVariable("variantId") Long variantId,
                                                          @RequestHeader("X-User-Id") Long userId,
                                                          @RequestBody @Validated ReviewRequest reviewRequestDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(new ReviewResponseDto());
    }

    @Operation(summary = "상품 리뷰 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @NotFoundApiResponse
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<PageDto<ReviewResponseDto>> getReviewsByProductId(@PathVariable("productId") Long productId,
                                                                            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
        PageDto<ReviewResponseDto> result = reviewService.getReviewList(productId, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "상품 리뷰 삭제")
    @ApiResponse(responseCode = "204", description = "리뷰 삭제")
    @ForbiddenApiResponse @NotFoundApiResponse
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable("reviewId") Long reviewId,
                                             @RequestHeader("X-User-Id") Long userId){
        return ResponseEntity.noContent().build();
    }
}

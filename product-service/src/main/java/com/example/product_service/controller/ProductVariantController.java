package com.example.product_service.controller;

import com.example.product_service.controller.util.specification.annotation.AdminApi;
import com.example.product_service.controller.util.specification.annotation.BadRequestApiResponse;
import com.example.product_service.controller.util.specification.annotation.ForbiddenApiResponse;
import com.example.product_service.controller.util.specification.annotation.NotFoundApiResponse;
import com.example.product_service.dto.request.review.ReviewRequest;
import com.example.product_service.dto.request.variant.UpdateProductVariantRequest;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.dto.response.variant.OrderProductVariantResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.service.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/variants")
@Tag(name = "ProductVariant" , description = "상품 변형 관련 API")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    @Operation(summary = "리뷰 등록")
    @ApiResponse(responseCode = "201", description = "등록 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PostMapping("/{variantId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(@PathVariable("variantId") Long variantId,
                                                       @RequestHeader("X-User-Id") Long userId,
                                                       @RequestBody @Validated ReviewRequest request){
        ReviewResponse response = productVariantService.addReview(variantId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @AdminApi
    @Operation(summary = "상품 변형 수정")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PatchMapping("/{variantId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ProductVariantResponse> updateProductVariant(@PathVariable("variantId") Long variantId,
                                                                       @Validated @RequestBody UpdateProductVariantRequest request){
        ProductVariantResponse response = productVariantService.updateVariantById(variantId, request);
        return ResponseEntity.ok(response);
    }

    @AdminApi
    @Operation(summary = "상품 변형 삭제")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ForbiddenApiResponse @NotFoundApiResponse
    @DeleteMapping("/{variantId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProductVariant(@PathVariable("variantId") Long variantId){
        productVariantService.deleteVariantById(variantId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/by-ids")
    public ResponseEntity<List<OrderProductVariantResponse>> getVariantByIds(@RequestBody List<Long> variantIds){
        List<OrderProductVariantResponse> responses = productVariantService.getOrderVariantByIds(variantIds);
        return ResponseEntity.ok(responses);
    }
}

package com.example.product_service.controller;

import com.example.product_service.controller.util.specification.annotation.AdminApi;
import com.example.product_service.controller.util.specification.annotation.BadRequestApiResponse;
import com.example.product_service.controller.util.specification.annotation.ForbiddenApiResponse;
import com.example.product_service.controller.util.specification.annotation.NotFoundApiResponse;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.UpdateProductVariantRequest;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products/{productId}/variants")
@Tag(name = "ProductVariants" , description = "상품 변형 관련 API")
public class ProductVariantController {

    @AdminApi
    @Operation(summary = "상품 변형 추가")
    @ApiResponse(responseCode = "201", description = "추가 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ProductVariantResponse> addVariant(@PathVariable("productId") Long productId,
                                                             @RequestBody ProductVariantRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProductVariantResponse());
    }

    @AdminApi
    @Operation(summary = "상품 변형 수정")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PatchMapping("/{variantId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ProductVariantResponse> updateProductVariant(@PathVariable("productId") Long productId,
                                                                       @PathVariable("variantId") Long variantId,
                                                                       @RequestBody UpdateProductVariantRequest request){
        return ResponseEntity.ok(new ProductVariantResponse());
    }

    @AdminApi
    @Operation(summary = "상품 변형 삭제")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @DeleteMapping("/{variantId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProductVariant(@PathVariable("productId") Long productId,
                                                     @PathVariable("variantId") Long variantId){
        return ResponseEntity.noContent().build();
    }
}

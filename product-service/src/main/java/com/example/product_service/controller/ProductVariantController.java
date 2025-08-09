package com.example.product_service.controller;

import com.example.product_service.controller.util.specification.annotation.AdminApi;
import com.example.product_service.controller.util.specification.annotation.BadRequestApiResponse;
import com.example.product_service.controller.util.specification.annotation.ForbiddenApiResponse;
import com.example.product_service.controller.util.specification.annotation.NotFoundApiResponse;
import com.example.product_service.dto.request.variant.UpdateProductVariantRequest;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.service.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/variants")
@Tag(name = "ProductVariants" , description = "상품 변형 관련 API")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;

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
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @DeleteMapping("/{variantId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProductVariant(@PathVariable("variantId") Long variantId){
        return ResponseEntity.noContent().build();
    }
}

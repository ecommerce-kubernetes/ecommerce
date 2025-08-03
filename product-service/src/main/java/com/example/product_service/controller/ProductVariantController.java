package com.example.product_service.controller;

import com.example.product_service.controller.util.specification.annotation.AdminApi;
import com.example.product_service.controller.util.specification.annotation.BadRequestApiResponse;
import com.example.product_service.controller.util.specification.annotation.ForbiddenApiResponse;
import com.example.product_service.controller.util.specification.annotation.NotFoundApiResponse;
import com.example.product_service.dto.response.variant.AddVariantResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products/{productId}/variants")
@Tag(name = "ProductVariants" , description = "상품 변형 관련 API")
public class ProductVariantController {

    @AdminApi
    @Operation(summary = "상품 변형 추가")
    @ApiResponse(responseCode = "201", description = "추가 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PostMapping
    public ResponseEntity<AddVariantResponse> addVariant(@PathVariable("productId") Long productId){
        return ResponseEntity.status(HttpStatus.CREATED).body(new AddVariantResponse());
    }
}

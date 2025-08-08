package com.example.product_service.controller;

import com.example.product_service.controller.util.specification.annotation.AdminApi;
import com.example.product_service.controller.util.specification.annotation.BadRequestApiResponse;
import com.example.product_service.controller.util.specification.annotation.ForbiddenApiResponse;
import com.example.product_service.controller.util.specification.annotation.NotFoundApiResponse;
import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.service.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "ProductImage" , description = "상품 이미지 관련 API")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @AdminApi
    @Operation(summary = "상품 이미지 추가")
    @ApiResponse(responseCode = "201", description = "추가 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PostMapping("/products/{productId}/images")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<ImageResponse>> addImage(@PathVariable("productId") Long productId,
                                                        @RequestBody AddImageRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(List.of(new ImageResponse()));
    }

    @AdminApi
    @Operation(summary = "상품 이미지 수정")
    @ApiResponse(responseCode = "200", description = "수정 완료")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PatchMapping("/product-images/{imageId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ImageResponse> updateImage(@PathVariable("imageId") Long imageId,
                                                     @RequestBody ImageRequest request){
        return ResponseEntity.ok(new ImageResponse());
    }

    @AdminApi
    @Operation(summary = "상품 이미지 삭제")
    @ApiResponse(responseCode = "204", description = "상품 이미지 삭제")
    @ForbiddenApiResponse @NotFoundApiResponse
    @DeleteMapping("/product-images/{imageId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteImage(@PathVariable("imageId") Long imageId){
        return ResponseEntity.noContent().build();
    }
}

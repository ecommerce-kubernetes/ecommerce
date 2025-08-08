package com.example.product_service.controller;

import com.example.product_service.controller.util.specification.annotation.AdminApi;
import com.example.product_service.controller.util.specification.annotation.BadRequestApiResponse;
import com.example.product_service.controller.util.specification.annotation.ForbiddenApiResponse;
import com.example.product_service.controller.util.specification.annotation.NotFoundApiResponse;
import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.dto.response.product.*;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@Tag(name = "Product" , description = "상품 관련 API")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @AdminApi
    @Operation(summary = "상품 저장")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Validated ProductRequest request){
        ProductResponse response = productService.saveProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @AdminApi
    @Operation(summary = "상품 이미지 추가")
    @ApiResponse(responseCode = "201", description = "추가 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PostMapping("/{productId}/images")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<ImageResponse>> addImages(@PathVariable("productId") Long productId,
                                                         @Validated  @RequestBody AddImageRequest request){
        List<ImageResponse> response = productService.addImages(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @AdminApi
    @Operation(summary = "상품 변형 추가")
    @ApiResponse(responseCode = "201", description = "추가 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PostMapping("/{productId}/variants")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ProductVariantResponse> addVariant(@PathVariable("productId") Long productId,
                                                             @RequestBody ProductVariantRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProductVariantResponse());
    }

    @Operation(summary = "상품 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @BadRequestApiResponse
    @GetMapping
    public ResponseEntity<PageDto<ProductSummaryResponse>> getProducts(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @Validated @ModelAttribute ProductSearch search){

        PageDto<ProductSummaryResponse> response = productService.getProducts(pageable, search);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상품 상세 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @NotFoundApiResponse
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable("productId") Long productId){
        ProductResponse response = productService.getProductById(productId);
        return ResponseEntity.ok(response);
    }

    //TODO 상품 조회와 통합되어 삭제 예상
    @Operation(summary = "특가 상품 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/special-sale")
    public ResponseEntity<PageDto<ProductSummaryResponse>> specialSaleProducts(@RequestParam(name = "page", defaultValue = "0") int page,
                                                                          @RequestParam(name = "size", defaultValue = "10") int size,
                                                                          @RequestParam(name = "categoryId", required = false) Long categoryId){
        return ResponseEntity.ok(new PageDto<>(List.of(new ProductSummaryResponse()), 0, 5, 10, 50));
    }

    @Operation(summary = "인기 상품 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/popular")
    public ResponseEntity<PageDto<ProductSummaryResponse>> getPopularProducts(@RequestParam(name = "page", defaultValue = "0") int page,
                                                                      @RequestParam(name = "size", defaultValue = "10") int size,
                                                                      @RequestParam(name = "categoryId", required = false) Long categoryId){
        PageDto<ProductSummaryResponse> response = productService.getPopularProducts(page, size, categoryId);
        return ResponseEntity.ok(response);
    }

    @AdminApi
    @Operation(summary = "상품 기본 정보 수정")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PatchMapping("/{productId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ProductUpdateResponse> updateBasicInfo(@PathVariable("productId") Long productId,
                                                                 @Validated @RequestBody UpdateProductBasicRequest request){
        ProductUpdateResponse response = productService.updateBasicInfoById(productId, request);
        return ResponseEntity.ok(response);
    }

    @AdminApi
    @Operation(summary = "상품 삭제")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @NotFoundApiResponse
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") Long productId){
        productService.deleteProductById(productId);
        return ResponseEntity.noContent().build();
    }
}

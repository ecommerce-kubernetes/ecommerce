package com.example.product_service.controller;

import com.example.product_service.controller.util.SortFieldValidator;
import com.example.product_service.controller.util.specification.annotation.AdminApi;
import com.example.product_service.controller.util.specification.annotation.BadRequestApiResponse;
import com.example.product_service.controller.util.specification.annotation.ForbiddenApiResponse;
import com.example.product_service.controller.util.specification.annotation.NotFoundApiResponse;
import com.example.product_service.dto.request.*;
import com.example.product_service.dto.request.options.IdsRequestDto;
import com.example.product_service.dto.request.product.CreateVariantsRequestDto;
import com.example.product_service.dto.request.product.ProductBasicRequestDto;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.product.ProductRequestDto;
import com.example.product_service.dto.response.CompactProductResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.product.*;
import com.example.product_service.entity.Products;
import com.example.product_service.service.ProductImageService;
import com.example.product_service.service.ProductService;
import com.example.product_service.service.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
//        ProductResponseDto response = productService.saveProduct(productRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProductResponse());
    }

    @Operation(summary = "상품 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @BadRequestApiResponse @NotFoundApiResponse
    @GetMapping
    public ResponseEntity<PageDto<ProductSummaryResponse>> getProducts(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "rating", required = false) Integer rating){
//        sortFieldValidator.validateSortFields(pageable.getSort(), Products.class, null);
//        PageDto<ProductSummaryDto> productList = productService.getProductList(pageable, categoryId, name, rating);
        return ResponseEntity.ok(new PageDto<>(List.of(new ProductSummaryResponse()), 0, 5, 10, 50));
    }

    @Operation(summary = "상품 상세 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @NotFoundApiResponse
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("productId") Long productId){
//        ProductResponseDto productDetails = productService.getProductDetails(productId);
        return ResponseEntity.ok(new ProductResponse());
    }

    @Operation(summary = "특가 상품 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/special-sale")
    public ResponseEntity<PageDto<ProductSummaryResponse>> specialSaleProducts(@RequestParam(name = "page", defaultValue = "0") int page,
                                                                          @RequestParam(name = "size", defaultValue = "10") int size,
                                                                          @RequestParam(name = "categoryId", required = false) Long categoryId){
        Pageable pageable = PageRequest.of(page, size);
        PageDto<ProductSummaryDto> result = productService.getSpecialSale(pageable, categoryId);
        return ResponseEntity.ok(new PageDto<>(List.of(new ProductSummaryResponse()), 0, 5, 10, 50));
    }

    @Operation(summary = "인기 상품 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/popular")
    public ResponseEntity<PageDto<ProductSummaryDto>> popularProducts(@RequestParam(name = "page", defaultValue = "0") int page,
                                                                      @RequestParam(name = "size", defaultValue = "10") int size,
                                                                      @RequestParam(name = "categoryId", required = false) Long categoryId){
        Pageable pageable = PageRequest.of(page, size);
        PageDto<ProductSummaryDto> result = productService.getPopularProductList(pageable, categoryId);
        return ResponseEntity.ok(result);
    }

    @AdminApi
    @Operation(summary = "상품 기본 정보 수정")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PatchMapping("/{productId}")
    public ResponseEntity<ProductUpdateResponse> updateBasicInfo(@PathVariable("productId") Long productId,
                                                                 @RequestBody ProductBasicRequestDto requestDto){
//        ProductResponseDto responseDto = productService.modifyProductBasic(productId, requestDto);
        return ResponseEntity.ok(new ProductUpdateResponse());
    }

    @AdminApi
    @Operation(summary = "상품 삭제")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @NotFoundApiResponse
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeProduct(@PathVariable("productId") Long productId){
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }


//    //TODO 삭제 예정
////    @PostMapping("/{productId}/image")
//    public ResponseEntity<ProductResponseDto> addProductImg(@PathVariable("productId") Long productId,
//                                                            @RequestBody @Validated ProductImageRequestDto productImageRequestDto){
//        ProductResponseDto productResponseDto = productImageService.addImage(productId, productImageRequestDto);
//        return ResponseEntity.status(HttpStatus.CREATED).body(productResponseDto);
//    }
//
//    //TODO 삭제 예정
////    @DeleteMapping("/image/{imageId}")
//    public ResponseEntity<Void> deleteProductImage(@PathVariable("imageId") Long imageId){
//        productImageService.deleteImage(imageId);
//        return ResponseEntity.noContent().build();
//    }
//
//    //TODO 삭제 예정
////    @PatchMapping("/image/{imageId}/sort")
//    public ResponseEntity<ProductResponseDto> changeImgOrder(@PathVariable("imageId") Long imageId,
//                                                             @RequestBody @Validated ImageOrderRequestDto requestDto){
//        ProductResponseDto responseDto = productImageService.imgSwapOrder(imageId, requestDto);
//        return ResponseEntity.ok(responseDto);
//    }
//
//    //TODO 삭제 예정
////    @PostMapping("/batch-delete")
//    public ResponseEntity<Void> productBatchDelete(@Validated @RequestBody IdsRequestDto requestDto){
//        productService.batchDeleteProducts(requestDto);
//        return ResponseEntity.noContent().build();
//    }
//
//    //TODO 삭제 예정
////    @PostMapping("/{productId}/variants")
//    public ResponseEntity<ProductResponseDto> addVariants(@PathVariable("productId") Long productId,
//                                                          @RequestBody CreateVariantsRequestDto requestDto){
//        ProductResponseDto responseDto = productVariantService.addVariants(productId, requestDto);
//        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
//    }
//
//    //TODO 삭제 예정
////    @DeleteMapping("/variants/{variantId}")
//    public ResponseEntity<ProductResponseDto> deleteVariants(@PathVariable("variantId") Long variantId){
//        productVariantService.deleteVariant(variantId);
//        return ResponseEntity.noContent().build();
//    }
//
//    //TODO
//    // 변경해야하는 API
//    @PatchMapping("/{productId}/stock")
//    public ResponseEntity<ProductResponseDto> updateProductStockQuantity(@PathVariable("productId") Long productId,
//                                                                         @RequestBody @Validated StockQuantityRequestDto stockQuantityRequestDto){
//        ProductResponseDto productResponseDto =
//                productService.modifyStockQuantity(productId, stockQuantityRequestDto);
//        return ResponseEntity.ok(productResponseDto);
//    }
//
//    //TODO 삭제 예정
//    @PostMapping("/lookup-by-ids")
//    public ResponseEntity<List<CompactProductResponseDto>> getProductsByIdBatch(@RequestBody ProductRequestIdsDto productRequestIdsDto){
//        List<CompactProductResponseDto> productListByIds = productService.getProductListByIds(productRequestIdsDto);
//        return ResponseEntity.ok(productListByIds);
//    }

}

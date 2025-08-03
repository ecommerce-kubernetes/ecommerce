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
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductResponseDto;
import com.example.product_service.dto.response.product.ProductSummaryDto;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@Tag(name = "Product" , description = "상품 관련 API")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductImageService productImageService;
    private final ProductVariantService productVariantService;
    private final SortFieldValidator sortFieldValidator;

    @AdminApi
    @Operation(summary = "상품 저장")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Validated ProductRequest request){
//        ProductResponseDto response = productService.saveProduct(productRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProductResponse());
    }

    @GetMapping
    public ResponseEntity<PageDto<ProductSummaryDto>> getAllProducts(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "rating", required = false) Integer rating){
        sortFieldValidator.validateSortFields(pageable.getSort(), Products.class, null);
        PageDto<ProductSummaryDto> productList = productService.getProductList(pageable, categoryId, name, rating);
        return ResponseEntity.ok(productList);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable("productId") Long productId){
        ProductResponseDto productDetails = productService.getProductDetails(productId);
        return ResponseEntity.ok(productDetails);
    }

    @PatchMapping("/{productId}/basic")
    public ResponseEntity<ProductResponseDto> updateBasicInfo(@PathVariable("productId") Long productId,
                                                              @RequestBody ProductBasicRequestDto requestDto){
        ProductResponseDto responseDto = productService.modifyProductBasic(productId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/{productId}/image")
    public ResponseEntity<ProductResponseDto> addProductImg(@PathVariable("productId") Long productId,
                                                            @RequestBody @Validated ProductImageRequestDto productImageRequestDto){
        ProductResponseDto productResponseDto = productImageService.addImage(productId, productImageRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponseDto);
    }

    @DeleteMapping("/image/{imageId}")
    public ResponseEntity<Void> deleteProductImage(@PathVariable("imageId") Long imageId){
        productImageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/image/{imageId}/sort")
    public ResponseEntity<ProductResponseDto> changeImgOrder(@PathVariable("imageId") Long imageId,
                                                             @RequestBody @Validated ImageOrderRequestDto requestDto){
        ProductResponseDto responseDto = productImageService.imgSwapOrder(imageId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeProduct(@PathVariable("productId") Long productId){
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batch-delete")
    public ResponseEntity<Void> productBatchDelete(@Validated @RequestBody IdsRequestDto requestDto){
        productService.batchDeleteProducts(requestDto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/variants")
    public ResponseEntity<ProductResponseDto> addVariants(@PathVariable("productId") Long productId,
                                                          @RequestBody CreateVariantsRequestDto requestDto){
        ProductResponseDto responseDto = productVariantService.addVariants(productId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @DeleteMapping("/variants/{variantId}")
    public ResponseEntity<ProductResponseDto> deleteVariants(@PathVariable("variantId") Long variantId){
        productVariantService.deleteVariant(variantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<PageDto<ProductSummaryDto>> popularProducts(@RequestParam(name = "page", defaultValue = "0") int page,
                                                                      @RequestParam(name = "size", defaultValue = "10") int size,
                                                                      @RequestParam(name = "categoryId", required = false) Long categoryId){
        Pageable pageable = PageRequest.of(page, size);
        PageDto<ProductSummaryDto> result = productService.getPopularProductList(pageable, categoryId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/special-sale")
    public ResponseEntity<PageDto<ProductSummaryDto>> specialSaleProducts(@RequestParam(name = "page", defaultValue = "0") int page,
                                                                          @RequestParam(name = "size", defaultValue = "10") int size,
                                                                          @RequestParam(name = "categoryId", required = false) Long categoryId){
        Pageable pageable = PageRequest.of(page, size);
        PageDto<ProductSummaryDto> result = productService.getSpecialSale(pageable, categoryId);
        return ResponseEntity.ok(result);
    }

    //TODO
    // 변경해야하는 API
    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ProductResponseDto> updateProductStockQuantity(@PathVariable("productId") Long productId,
                                                                         @RequestBody @Validated StockQuantityRequestDto stockQuantityRequestDto){
        ProductResponseDto productResponseDto =
                productService.modifyStockQuantity(productId, stockQuantityRequestDto);
        return ResponseEntity.ok(productResponseDto);
    }

    @PostMapping("/lookup-by-ids")
    public ResponseEntity<List<CompactProductResponseDto>> getProductsByIdBatch(@RequestBody ProductRequestIdsDto productRequestIdsDto){
        List<CompactProductResponseDto> productListByIds = productService.getProductListByIds(productRequestIdsDto);
        return ResponseEntity.ok(productListByIds);
    }

}

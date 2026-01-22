package com.example.product_service.api.product.controller;

import com.example.product_service.api.common.dto.PageDto;
import com.example.product_service.api.product.controller.dto.*;
import com.example.product_service.api.product.service.ProductService;
import com.example.product_service.api.product.service.dto.command.ProductCreateCommand;
import com.example.product_service.api.product.service.dto.command.ProductUpdateCommand;
import com.example.product_service.api.product.service.dto.command.ProductVariantsCreateCommand;
import com.example.product_service.api.product.service.dto.result.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductCreateResponse> createProduct(@RequestBody @Validated ProductCreateRequest request) {
        ProductCreateCommand command = ProductCreateCommand.builder()
                .name(request.getName())
                .categoryId(request.getCategoryId())
                .description(request.getDescription())
                .build();

        ProductCreateResponse response = productService.createProduct(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{productId}/option")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductOptionResponse> registerProductOption(@PathVariable("productId") Long productId,
                                                                       @RequestBody @Validated ProductOptionRequest request) {
        ProductOptionResponse response = productService.defineOptions(productId, request.getOptionTypeIds());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productId}/variants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VariantCreateResponse> createVariants(@PathVariable("productId") Long productId,
                                                                @RequestBody @Validated VariantCreateRequest request) {
        List<ProductVariantsCreateCommand.VariantDetail> variantDetails = request.getVariants().stream().map(v -> ProductVariantsCreateCommand.VariantDetail.builder()
                .originalPrice(v.getOriginalPrice())
                .discountRate(v.getDiscountRate())
                .stockQuantity(v.getStockQuantity())
                .optionValueIds(v.getOptionValueIds())
                .build()).toList();

        ProductVariantsCreateCommand command = ProductVariantsCreateCommand.builder()
                .productId(productId)
                .variants(variantDetails)
                .build();

        VariantCreateResponse response = productService.createVariants(command);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductImageCreateResponse> updateImages(@PathVariable("productId") Long productId,
                                                                   @RequestBody @Validated ProductImageCreateRequest request) {
        ProductImageCreateResponse response = productService.updateImages(productId, request.getImages());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productId}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductStatusResponse> publishProduct(@PathVariable("productId") Long productId) {
        ProductStatusResponse response = productService.publish(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageDto<ProductSummaryResponse>> getProducts(@ModelAttribute @Validated ProductSearchCondition condition) {
        PageDto<ProductSummaryResponse> response = productService.getProducts(condition);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable("productId") Long productId) {
        ProductDetailResponse response = productService.getProduct(productId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductUpdateResponse> updateProduct(@PathVariable("productId") Long productId,
                                                               @RequestBody @Validated ProductUpdateRequest request) {
        ProductUpdateCommand command = ProductUpdateCommand.builder()
                .productId(productId)
                .name(request.getName())
                .categoryId(request.getCategoryId())
                .description(request.getDescription())
                .build();

        ProductUpdateResponse response = productService.updateProduct(command);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductStatusResponse> closeProduct(@PathVariable("productId") Long productId) {
        ProductStatusResponse response = productService.closedProduct(productId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}

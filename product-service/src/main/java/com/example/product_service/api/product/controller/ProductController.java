package com.example.product_service.api.product.controller;

import com.example.product_service.api.product.controller.dto.*;
import com.example.product_service.api.product.service.dto.*;
import com.example.product_service.api.product.service.ProductService;
import com.example.product_service.dto.response.PageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductCreateResponse> productDraft(@RequestBody @Validated ProductCreateRequest request) {
        return null;
    }

    @PostMapping("/{productId}/option-specs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OptionSpecResponse> productOptionSpec(@PathVariable("productId") Long productId,
                                                                @RequestBody @Validated OptionSpecRequest request) {
        return null;
    }

    @PostMapping("/{productId}/variants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VariantCreateResponse> addVariant(@PathVariable("productId") Long productId,
                                                            @RequestBody @Validated VariantCreateRequest request) {
        return null;
    }

    @PostMapping("/{productId}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImageCreateResponse> addImage(@PathVariable("productId") Long productId,
                                                        @RequestBody @Validated ProductImageCreateRequest request) {
        return null;
    }

    @PatchMapping("/{productId}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductPublishResponse> publish(@PathVariable("productId") Long productId) {
        return null;
    }

    @GetMapping
    public ResponseEntity<PageDto<ProductSummaryResponse>> getProducts(@ModelAttribute @Validated ProductSearchCondition condition) {
        return null;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProduct(@PathVariable("productId") Long productId) {
        return null;
    }
}

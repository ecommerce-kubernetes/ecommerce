package com.example.product_service.product.adapter.in.web;

import com.example.product_service.common.dto.PageDto;
import com.example.product_service.product.adapter.in.web.dto.request.SearchProductCondition;
import com.example.product_service.product.adapter.in.web.dto.response.ProductDetailResponse;
import com.example.product_service.product.adapter.in.web.dto.response.ProductSummaryResponse;
import com.example.product_service.product.application.service.ProductQueryService;
import com.example.product_service.product.application.service.dto.command.SearchProductCommand;
import com.example.product_service.product.application.service.dto.result.ProductDetailResult;
import com.example.product_service.product.application.service.dto.result.ProductSummaryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductQueryService productQueryService;

    @GetMapping
    public ResponseEntity<PageDto<ProductSummaryResponse>> getProducts(
            @ModelAttribute SearchProductCondition condition,
            @PageableDefault(size = 20, page = 0, sort = {}) Pageable pageable) {
        SearchProductCommand command = condition.toCommand(pageable);
        Page<ProductSummaryResult> results = productQueryService.getProducts(command);
        PageDto<ProductSummaryResponse> response = PageDto.of(results, ProductSummaryResponse::from);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProduct(@PathVariable("productId") Long productId) {
        ProductDetailResult result = productQueryService.getProduct(productId);
        return ResponseEntity.ok(ProductDetailResponse.from(result));
    }
}

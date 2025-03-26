package com.example.product_service.controller;

import com.example.product_service.dto.request.ProductRequestDto;
import com.example.product_service.dto.request.StockQuantityRequestDto;
import com.example.product_service.dto.response.ProductResponseDto;
import com.example.product_service.service.ProductService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody @Validated ProductRequestDto productRequestDto){
        ProductResponseDto product = productService.saveProduct(productRequestDto);
        return ResponseEntity.status(HttpServletResponse.SC_CREATED).body(product);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeProduct(@PathVariable("productId") Long productId){
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ProductResponseDto> updateProductStockQuantity(@PathVariable("productId") Long productId,
                                                                         @RequestBody @Validated StockQuantityRequestDto stockQuantityRequestDto){
        ProductResponseDto productResponseDto =
                productService.modifyStockQuantity(productId, stockQuantityRequestDto);
        return ResponseEntity.ok(productResponseDto);
    }
}

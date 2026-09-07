package com.example.product_service.product.adapter.in.web;

import com.example.product_service.product.adapter.in.web.dto.request.*;
import com.example.product_service.product.adapter.in.web.dto.response.*;
import com.example.product_service.product.application.service.ProductCommandService;
import com.example.product_service.product.application.service.dto.command.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductCommandService productCommandService;

    @PostMapping
    public ResponseEntity<CreateProductResponse> createProduct(@RequestBody @Validated CreateProductRequest request) {
        CreateProductCommand command = request.toCommand();
        Long id = productCommandService.createProduct(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(CreateProductResponse.of(id));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<UpdateProductResponse> updateProduct(@PathVariable("productId") Long productId,
                                                               @RequestBody @Validated UpdateProductRequest request) {
        UpdateProductCommand command = request.toCommand(productId);
        Long id = productCommandService.updateProduct(command);
        return ResponseEntity.ok(UpdateProductResponse.of(id));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") Long productId) {
        productCommandService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{productId}/options")
    public ResponseEntity<RegisterProductOptionResponse> registerProductOptions(@PathVariable("productId") Long productId,
                                                                                @RequestBody @Validated RegisterProductOptionRequest request) {
        RegisterProductOptionCommand command = request.toCommand(productId);
        Long id = productCommandService.registerProductOptions(command);
        return ResponseEntity.ok(RegisterProductOptionResponse.of(id));
    }

    @PostMapping("/{productId}/variants")
    public ResponseEntity<AddProductVariantResponse> addProductVariants(@PathVariable("productId") Long productId,
                                                                        @RequestBody @Validated AddProductVariantRequest request) {
        AddProductVariantCommand command = request.toCommand(productId);
        Long id = productCommandService.addProductVariants(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(AddProductVariantResponse.of(id));
    }

    @PutMapping("/{productId}/images")
    public ResponseEntity<AddProductImageResponse> addProductImages(@PathVariable("productId") Long productId,
                                                                    @RequestBody @Validated AddProductImageRequest request) {
        AddProductImageCommand command = request.toCommand(productId);
        Long id = productCommandService.addProductImages(command);
        return ResponseEntity.ok(AddProductImageResponse.of(id));
    }

    @PutMapping("/{productId}/description-images")
    public ResponseEntity<AddProductDescriptionImageResponse> addProductDescriptionImages(@PathVariable("productId") Long productId,
                                                                                          @RequestBody @Validated AddProductDescriptionImageRequest request) {
        AddProductDescriptionImageCommand command = request.toCommand(productId);
        Long id = productCommandService.addProductDescriptionImages(command);
        return ResponseEntity.ok(AddProductDescriptionImageResponse.of(id));
    }
}

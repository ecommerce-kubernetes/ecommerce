package com.example.product_service.product.adapter.in.web;

import com.example.product_service.common.dto.PageDto;
import com.example.product_service.product.adapter.in.web.dto.request.*;
import com.example.product_service.product.adapter.in.web.dto.response.*;
import com.example.product_service.product.application.service.ProductService;
import com.example.product_service.product.application.service.dto.command.ProductCommand;
import com.example.product_service.product.application.service.dto.result.ProductResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<CreateProductResponse> createProduct(@RequestBody @Validated CreateProductRequest request) {

        ProductCommand.Create command = request.toCommand();
        ProductResult.Create result = productService.createProduct(command);
        CreateProductResponse response = CreateProductResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{productId}/options")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegisterProductOptionResponse> registerProductOption(@PathVariable("productId") Long productId,
                                                                                @RequestBody @Validated RegisterProductOptionRequest request) {
        ProductCommand.OptionRegister command = request.toCommand(productId);
        ProductResult.OptionRegister result = productService.defineOptions(command);
        RegisterProductOptionResponse response = RegisterProductOptionResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productId}/variants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddProductVariantResponse> addVariants(@PathVariable("productId") Long productId,
                                                  @RequestBody @Validated AddProductVariantRequest request) {
        ProductCommand.AddVariant command = request.toCommand(productId);
        ProductResult.AddVariant result = productService.createVariants(command);
        AddProductVariantResponse response = AddProductVariantResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddProductImageResponse> updateImages(@PathVariable("productId") Long productId,
                                                 @RequestBody @Validated AddProductImageRequest request) {
        ProductCommand.AddImage command = request.toCommand(productId);
        ProductResult.AddImage result = productService.updateImages(command);
        AddProductImageResponse response = AddProductImageResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}/description-images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddProductDescriptionImageResponse> updateDescriptionImage(@PathVariable("productId") Long productId,
                                                                      @RequestBody @Validated AddProductDescriptionImageRequest request) {
        ProductCommand.AddDescriptionImage command = request.toCommand(productId);
        ProductResult.AddDescriptionImage result = productService.updateDescriptionImages(command);
        AddProductDescriptionImageResponse response = AddProductDescriptionImageResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productId}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PublishProductResponse> publishProduct(@PathVariable("productId") Long productId) {
        ProductResult.Publish result = productService.publish(productId);
        PublishProductResponse response = PublishProductResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CloseProductResponse> closeProduct(@PathVariable("productId") Long productId) {
        ProductResult.Close result = productService.closedProduct(productId);
        CloseProductResponse response = CloseProductResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageDto<ProductSummaryResponse>> getProducts(@ModelAttribute @Validated SearchProductRequest condition) {
        ProductCommand.Search command = condition.toCommand();
        Page<ProductResult.Summary> results = productService.getProducts(command);
        PageDto<ProductSummaryResponse> response = PageDto.of(results, ProductSummaryResponse::from);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable("productId") Long productId) {
        ProductResult.Detail result = productService.getProduct(productId);
        ProductDetailResponse response = ProductDetailResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UpdateProductResponse> updateProduct(@PathVariable("productId") Long productId,
                                                               @RequestBody @Validated UpdateProductRequest request) {
        ProductCommand.Update command = request.toCommand(productId);
        ProductResult.Update result = productService.updateProduct(command);
        UpdateProductResponse response = UpdateProductResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}

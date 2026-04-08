package com.example.product_service.api.product.controller;

import com.example.product_service.api.common.dto.PageDto;
import com.example.product_service.api.product.controller.dto.ProductSearchCondition;
import com.example.product_service.api.product.controller.dto.request.ProductRequest;
import com.example.product_service.api.product.controller.dto.response.ProductResponse;
import com.example.product_service.api.product.service.ProductService;
import com.example.product_service.api.product.service.dto.command.ProductCommand;
import com.example.product_service.api.product.service.dto.result.ProductDetailResponse;
import com.example.product_service.api.product.service.dto.result.ProductResult;
import com.example.product_service.api.product.service.dto.result.ProductSummaryResponse;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<ProductResponse.Create> createProduct(@RequestBody @Validated ProductRequest.Create request) {

        ProductCommand.Create command = request.toCommand();
        ProductResult.Create result = productService.createProduct(command);
        ProductResponse.Create response = ProductResponse.Create.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{productId}/options")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse.OptionRegister> registerProductOption(@PathVariable("productId") Long productId,
                                                                                @RequestBody @Validated ProductRequest.OptionRegister request) {
        ProductCommand.OptionRegister command = request.toCommand(productId);
        ProductResult.OptionRegister result = productService.defineOptions(command);
        ProductResponse.OptionRegister response = ProductResponse.OptionRegister.from(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productId}/variants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse.AddVariant> addVariants(@PathVariable("productId") Long productId,
                                                  @RequestBody @Validated ProductRequest.AddVariant request) {
        ProductCommand.AddVariant command = request.toCommand(productId);
        ProductResult.AddVariant result = productService.createVariants(command);
        ProductResponse.AddVariant response = ProductResponse.AddVariant.from(result);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse.AddImage> updateImages(@PathVariable("productId") Long productId,
                                                 @RequestBody @Validated ProductRequest.AddImage request) {
        ProductCommand.AddImage command = request.toCommand(productId);
        ProductResult.AddImage result = productService.updateImages(command);
        ProductResponse.AddImage response = ProductResponse.AddImage.from(result);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}/description-images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse.AddDescriptionImage> updateDescriptionImage(@PathVariable("productId") Long productId,
                                                                      @RequestBody @Validated ProductRequest.AddDescriptionImage request) {
        ProductCommand.AddDescriptionImage command = request.toCommand(productId);
        ProductResult.AddDescriptionImage result = productService.updateDescriptionImages(command);
        ProductResponse.AddDescriptionImage response = ProductResponse.AddDescriptionImage.from(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productId}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse.Publish> publishProduct(@PathVariable("productId") Long productId) {
        ProductResult.Publish result = productService.publish(productId);
        ProductResponse.Publish response = ProductResponse.Publish.from(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse.Close> closeProduct(@PathVariable("productId") Long productId) {
        ProductResult.Close result = productService.closedProduct(productId);
        ProductResponse.Close response = ProductResponse.Close.from(result);
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
    public ResponseEntity<ProductResponse.Update> updateProduct(@PathVariable("productId") Long productId,
                                                               @RequestBody @Validated ProductRequest.Update request) {
        ProductCommand.Update command = request.toCommand(productId);
        ProductResult.Update result = productService.updateProduct(command);
        ProductResponse.Update response = ProductResponse.Update.from(result);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}

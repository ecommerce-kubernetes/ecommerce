package com.example.product_service.api.product.controller;

import com.example.product_service.api.common.dto.PageDto;
import com.example.product_service.api.product.controller.dto.ProductSearchCondition;
import com.example.product_service.api.product.controller.dto.request.ProductRequest;
import com.example.product_service.api.product.controller.dto.request.ProductRequest.*;
import com.example.product_service.api.product.controller.dto.response.ProductResponse.*;
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
    public ResponseEntity<CreateResponse> createProduct(@RequestBody @Validated CreateRequest request) {
        ProductCreateCommand command = request.toCommand();
        ProductCreateResult result = productService.createProduct(command);
        CreateResponse response = CreateResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{productId}/options")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OptionRegisterResponse> registerProductOption(@PathVariable("productId") Long productId,
                                                                        @RequestBody @Validated OptionRegisterRequest request) {
        //TODO 옵션 Command 객체 넘기는 방식으로 변경
        List<Long> list = request.options().stream().map(ProductRequest.ProductOptionRequest::optionTypeId).toList();
        ProductOptionResponse result = productService.defineOptions(productId, list);
        OptionRegisterResponse response = OptionRegisterResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productId}/variants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddVariantResponse> addVariants(@PathVariable("productId") Long productId,
                                                          @RequestBody @Validated AddVariantRequest request) {
        ProductVariantsCreateCommand command = request.toCommand(productId);
        AddVariantResult result = productService.createVariants(command);
        AddVariantResponse response = AddVariantResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddImageResponse> updateImages(@PathVariable("productId") Long productId,
                                                         @RequestBody @Validated AddImageRequest request) {
        //TODO command 객체 사용
        List<String> images = request.images().stream().map((image) -> image.imagePath()).toList();
        ProductImageCreateResult result = productService.updateImages(productId, images);
        AddImageResponse response = AddImageResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}/description-images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddDescriptionImageResponse> updateDescriptionImage(@PathVariable("productId") Long productId,
                                                                           @RequestBody @Validated AddDescriptionImageRequest request) {
        //TODO command 객체로 변경
        List<String> list = request.images().stream().map((image) -> image.imagePath()).toList();
        ProductDescriptionImageResult result = productService.updateDescriptionImages(productId, list);
        AddDescriptionImageResponse response = AddDescriptionImageResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productId}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PublishResponse> publishProduct(@PathVariable("productId") Long productId) {
        ProductStatusResult result = productService.publish(productId);
        PublishResponse response = PublishResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CloseResponse> closeProduct(@PathVariable("productId") Long productId) {
        ProductStatusResult result = productService.closedProduct(productId);
        CloseResponse response = CloseResponse.from(result);
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
                                                               @RequestBody @Validated UpdateRequest request) {

        ProductUpdateCommand command = request.toCommand(productId);
        ProductUpdateResponse response = productService.updateProduct(command);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}

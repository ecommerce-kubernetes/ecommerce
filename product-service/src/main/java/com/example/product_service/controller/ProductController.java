package com.example.product_service.controller;

import com.example.product_service.controller.util.SortFieldValidator;
import com.example.product_service.dto.request.*;
import com.example.product_service.dto.request.product.ProductRequestDto;
import com.example.product_service.dto.response.CompactProductResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.product.ProductResponseDto;
import com.example.product_service.entity.Products;
import com.example.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final SortFieldValidator sortFieldValidator;

    // TODO 가장 하위의 카테고리에 속하도록 변경
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody @Validated ProductRequestDto productRequestDto){
        ProductResponseDto product = productService.saveProduct(productRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeProduct(@PathVariable("productId") Long productId){
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/image")
    public ResponseEntity<ProductResponseDto> addProductImg(@PathVariable("productId") Long productId,
                                           @RequestBody @Validated ProductImageRequestDto productImageRequestDto){
        ProductResponseDto productResponseDto = productService.addImage(productId, productImageRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(productResponseDto);
    }

    @PatchMapping("/{productId}/image/sort")
    public ResponseEntity<ProductResponseDto> changeImgOrder(@PathVariable("productId") Long productId,
                                                             @RequestBody @Validated ImageOrderRequestDto imageOrderRequestDto){
        ProductResponseDto productResponseDto = productService.imgSwapOrder(productId, imageOrderRequestDto);

        return ResponseEntity.ok(productResponseDto);
    }

    @DeleteMapping("/image/{imageId}")
    public ResponseEntity<Void> deleteProductImage(@PathVariable("imageId") Long imageId){
        productService.deleteImage(imageId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ProductResponseDto> updateProductStockQuantity(@PathVariable("productId") Long productId,
                                                                         @RequestBody @Validated StockQuantityRequestDto stockQuantityRequestDto){
        ProductResponseDto productResponseDto =
                productService.modifyStockQuantity(productId, stockQuantityRequestDto);
        return ResponseEntity.ok(productResponseDto);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable("productId") Long productId){
        ProductResponseDto productDetails = productService.getProductDetails(productId);
        return ResponseEntity.ok(productDetails);
    }

    @GetMapping
    public ResponseEntity<PageDto<ProductResponseDto>> getAllProducts(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "name", required = false) String name){
        sortFieldValidator.validateSortFields(pageable.getSort(), Products.class);
        PageDto<ProductResponseDto> productList = productService.getProductList(pageable, categoryId, name);
        return ResponseEntity.ok(productList);
    }

    @PostMapping("/lookup-by-ids")
    public ResponseEntity<List<CompactProductResponseDto>> getProductsByIdBatch(@RequestBody ProductRequestIdsDto productRequestIdsDto){
        List<CompactProductResponseDto> productListByIds = productService.getProductListByIds(productRequestIdsDto);
        return ResponseEntity.ok(productListByIds);
    }

}

package com.example.product_service.controller;

import com.example.product_service.dto.request.ProductRequestDto;
import com.example.product_service.dto.request.StockQuantityRequestDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ProductResponseDto;
import com.example.product_service.entity.Products;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.service.ProductService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable("productId") Long productId){
        ProductResponseDto productDetails = productService.getProductDetails(productId);
        return ResponseEntity.ok(productDetails);
    }

    @GetMapping
    public ResponseEntity<PageDto<ProductResponseDto>> getAllProducts(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
        validateSortFields(pageable.getSort(), Products.class);
        PageDto<ProductResponseDto> productList = productService.getProductList(pageable);
        return ResponseEntity.ok(productList);
    }

    private void validateSortFields(Sort sort, Class<?> entityClass){
        List<String> validFields = Arrays.stream(entityClass.getDeclaredFields())
                .map(Field::getName)
                .toList();
        for (Sort.Order order : sort){
            if(!validFields.contains(order.getProperty())){
                throw new BadRequestException(order.getProperty() + "is not Entity Field");
            }
        }
    }
}

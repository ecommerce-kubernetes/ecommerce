package com.example.order_service.client;

import com.example.order_service.dto.client.ProductResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${product.service.url}")
public interface ProductClient {
    @GetMapping("/products/{productId}")
    ProductResponseDto getProduct(@PathVariable("productId") Long productId);
}

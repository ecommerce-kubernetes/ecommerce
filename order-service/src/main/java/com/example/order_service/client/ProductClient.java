package com.example.order_service.client;

import com.example.order_service.dto.client.ProductRequestIdsDto;
import com.example.order_service.dto.client.CompactProductResponseDto;
import com.example.order_service.dto.client.ProductResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product-service", url = "${product.service.url}")
public interface ProductClient {
    @GetMapping("/products/{productId}")
    ProductResponseDto getProduct(@PathVariable("productId") Long productId);

    @PostMapping("/products/lookup-by-ids")
    List<CompactProductResponseDto> getProductsByIdBatch(@RequestBody ProductRequestIdsDto productRequestIdsDto);
}

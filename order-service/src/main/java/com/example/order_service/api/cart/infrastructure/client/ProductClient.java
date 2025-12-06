package com.example.order_service.api.cart.infrastructure.client;

import com.example.order_service.service.client.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "product-service")
public interface ProductClient {
    @GetMapping("/variants/{productVariantId}")
    ProductResponse getProductVariant(@PathVariable("productVariantId") Long productVariantId);

    @PostMapping("/variants/by-ids")
    List<ProductResponse> getProductVariantByIds(List<Long> ids);
}

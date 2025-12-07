package com.example.order_service.api.cart.infrastructure.client;

import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "product-service", contextId = "cartProductClient")
public interface CartProductClient {
    @GetMapping("/variants/{productVariantId}")
    CartProductResponse getProductByVariantId(@PathVariable("productVariantId") Long productVariantId);

    @PostMapping("/variants/by-ids")
    List<CartProductResponse> getProductVariantByIds(List<Long> ids);
}

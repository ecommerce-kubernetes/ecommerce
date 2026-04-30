package com.example.order_service.cart.infrastructure.client;

import com.example.order_service.common.client.product.ProductFeignConfig;
import com.example.order_service.cart.infrastructure.client.dto.CartProductResponse;
import com.example.order_service.cart.infrastructure.client.dto.CartProductsRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "product-service", contextId = "cartProductClient", configuration = ProductFeignConfig.class)
public interface CartProductClient {
    @GetMapping("/internal/variants/{productVariantId}")
    CartProductResponse getProductByVariantId(@PathVariable("productVariantId") Long productVariantId);

    @PostMapping("/internal/variants/by-ids")
    List<CartProductResponse> getProductVariantByIds(CartProductsRequest request);
}

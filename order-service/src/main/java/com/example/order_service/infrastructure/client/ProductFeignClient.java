package com.example.order_service.infrastructure.client;

import com.example.order_service.infrastructure.config.DefaultFeignConfig;
import com.example.order_service.infrastructure.dto.request.ProductClientRequest;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "product-service", contextId = "productClient", configuration = DefaultFeignConfig.class)
public interface ProductFeignClient {
    @PostMapping("/internal/variants/validate-for-order")
    ProductClientResponse.ProductList getProductsForOrder(ProductClientRequest.Validate request);

    @PostMapping("/internal/variants/validate-for-cart")
    ProductClientResponse.ProductList getProductsForCart(ProductClientRequest.Validate request);

    @PostMapping("/internal/variants")
    ProductClientResponse.ProductList getProducts(ProductClientRequest.BulkSearch request);
}

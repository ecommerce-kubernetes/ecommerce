package com.example.order_service.infrastructure.client;

import com.example.order_service.infrastructure.config.DefaultFeignConfig;
import com.example.order_service.infrastructure.dto.request.ProductClientRequest;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "product-service", contextId = "productClient", configuration = DefaultFeignConfig.class)
public interface ProductFeignClient {
    @PostMapping("/internal/variants/by-ids")
    List<ProductClientResponse.ProductDeprecated> getProductsByVariantIds(ProductClientRequest.ProductVariantIds req);

    @PostMapping("/internal/variants/validate-for-order")
    List<ProductClientResponse.Product> getProductsForOrder(ProductClientRequest.Validate request);

    @PostMapping("/internal/variants/validate-for-cart")
    List<ProductClientResponse.Product> getProductsForCart(ProductClientRequest.Validate request);
}

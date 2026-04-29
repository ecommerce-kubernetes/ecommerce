package com.example.order_service.infrastructure.client;

import com.example.order_service.infrastructure.config.ProductFeignConfig;
import com.example.order_service.infrastructure.dto.request.ProductClientRequest;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "product-service", contextId = "productClient", configuration = ProductFeignConfig.class)
public interface ProductFeignClient {
    @PostMapping("/internal/variants/by-ids")
    List<ProductClientResponse.Product> getProductsByVariantIds(ProductClientRequest.ProductVariantIds req);
}

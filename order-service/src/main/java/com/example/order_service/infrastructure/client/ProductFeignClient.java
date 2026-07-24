package com.example.order_service.infrastructure.client;

import com.example.order_service.infrastructure.config.DefaultFeignConfig;
import com.example.order_service.infrastructure.dto.request.ProductBulkSearchRequest;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "product-service", contextId = "productClient", configuration = DefaultFeignConfig.class)
public interface ProductFeignClient {

    @PostMapping("/internal/products/search")
    ProductResponse getProducts(ProductBulkSearchRequest request);

}

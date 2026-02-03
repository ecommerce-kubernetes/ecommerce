package com.example.order_service.api.order.infrastructure.client.product;

import com.example.order_service.api.common.client.product.ProductFeignConfig;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductsRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "product-service", contextId = "orderProductClient", configuration = ProductFeignConfig.class)
public interface OrderProductClient {
    @PostMapping("/internal/variants/by-ids")
    List<OrderProductResponse> getProductVariantByIds(OrderProductsRequest request);
}

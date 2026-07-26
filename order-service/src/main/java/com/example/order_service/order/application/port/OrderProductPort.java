package com.example.order_service.order.application.port;

import com.example.order_service.order.application.port.dto.result.OrderProductsResult;

import java.util.List;

public interface OrderProductPort {
    OrderProductsResult getProducts(List<Long> productVariantIds);
}

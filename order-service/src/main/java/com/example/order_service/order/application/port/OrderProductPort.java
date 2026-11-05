package com.example.order_service.order.application.port;

import com.example.order_service.order.application.port.dto.OrderProductsResult;

import java.util.List;

public interface OrderProductPort {
    OrderProductsResult getProducts(List<Long> productVariantIds);
}

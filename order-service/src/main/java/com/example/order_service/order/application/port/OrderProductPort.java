package com.example.order_service.order.application.port;

import com.example.order_service.order.application.port.dto.result.OrderProductResult;

import java.util.List;

public interface OrderProductPort {
    OrderProductResult getProducts(List<Long> productVariantIds);
}

package com.example.order_service.order.application.port;

import com.example.order_service.order.application.port.dto.OrderCartItemsResult;

import java.util.List;

public interface OrderCartPort {
    OrderCartItemsResult getCartItems(Long userId, List<Long> cartItemIds);
}

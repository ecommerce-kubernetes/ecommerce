package com.example.order_service.order.application.port;

import com.example.order_service.order.application.port.dto.OrdererPointResult;
import com.example.order_service.order.application.port.dto.OrdererProfileResult;

public interface OrderUserPort {
    OrdererProfileResult getOrdererProfile(Long userId);

    OrdererPointResult getOrdererPoints(Long userId);
}

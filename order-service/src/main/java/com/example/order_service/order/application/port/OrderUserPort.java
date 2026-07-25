package com.example.order_service.order.application.port;

import com.example.order_service.order.application.port.dto.result.OrdererPointResult;
import com.example.order_service.order.application.port.dto.result.OrdererProfileResult;

public interface OrderUserPort {
    OrdererProfileResult getOrdererProfile(Long userId);

    OrdererPointResult getOrdererPoints(Long userId);
}

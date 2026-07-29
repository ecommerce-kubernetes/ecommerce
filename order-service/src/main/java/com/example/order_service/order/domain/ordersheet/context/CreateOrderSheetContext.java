package com.example.order_service.order.domain.ordersheet.context;

import com.example.order_service.order.domain.vo.Orderer;

import java.time.LocalDateTime;

public record CreateOrderSheetContext(
        Orderer orderer,
        LocalDateTime expiresAt
) {
}

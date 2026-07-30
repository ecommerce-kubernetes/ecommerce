package com.example.order_service.order.domain.ordersheet.context;

import com.example.order_service.order.domain.vo.Orderer;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CreateOrderSheetContext(
        Orderer orderer,
        List<CreateOrderSheetItemContext> items,
        LocalDateTime expiresAt
) {
}

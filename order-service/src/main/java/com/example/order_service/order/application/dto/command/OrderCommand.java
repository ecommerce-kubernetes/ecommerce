package com.example.order_service.order.application.dto.command;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

public class OrderCommand {

    @Builder
    public record Create (
            Long userId,
            Long orderSheetId,
            Delivery deliveryAddress,
            Long couponId,
            Money pointToUse,
            Money expectedPrice
    ) {
    }

    @Builder
    public record Delivery (
            String receiverName,
            String receiverPhone,
            String zipCode,
            String baseAddress,
            String detailAddress
    ) {
    }
}

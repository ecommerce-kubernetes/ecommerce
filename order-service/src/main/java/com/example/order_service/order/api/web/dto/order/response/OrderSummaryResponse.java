package com.example.order_service.order.api.web.dto.order.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderSummaryResponse(
        Long orderId,
        String status,
        List<OrderItemResponse> orderItems,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        LocalDateTime createdAt
) {

    @Builder
    public record OrderItemResponse(
            Long orderItemId,
            ProductInfo product,
            List<OptionInfo> options,
            Integer quantity,
            ItemPaymentResponse itemPayment
    ) {
    }

    @Builder
    public record ProductInfo(
            Long productId,
            Long productVariantId,
            String sku,
            String productName,
            String thumbnail
    ) {
    }

    @Builder
    public record OptionInfo(
            String optionTypeName,
            String optionValueName
    ) {
    }

    @Builder
    public record ItemPaymentResponse(
            Long lineTotal,
            Long couponDiscount,
            Long finalItemAmount
    ) {
    }
}

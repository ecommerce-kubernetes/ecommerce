package com.example.order_service.api.order.controller.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemRequest {
    @NotNull(message = "productVariantId는 필수입니다")
    private Long productVariantId;
    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1이상이여야 합니다")
    private Integer quantity;

    @Builder
    private OrderItemRequest(Long productVariantId, Integer quantity){
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }
}

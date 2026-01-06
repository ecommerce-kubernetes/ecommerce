package com.example.order_service.api.order.controller.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateOrderItemRequest {
    @NotNull(message = "productVariantId는 필수입니다")
    private Long productVariantId;
    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1이상이여야 합니다")
    private Integer quantity;

    @Builder
    private CreateOrderItemRequest(Long productVariantId, Integer quantity){
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }
}

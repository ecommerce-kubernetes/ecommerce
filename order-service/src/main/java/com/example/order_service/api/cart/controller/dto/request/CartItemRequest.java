package com.example.order_service.api.cart.controller.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class CartItemRequest {
    @NotNull(message = "productVariantId는 필수값입니다")
    private Long productVariantId;
    @NotNull(message = "quantity는 필수값입니다")
    @Min(value = 1, message = "quantity는 1이상 이여야 합니다")
    private Integer quantity;

    @Builder
    private CartItemRequest(Long productVariantId, Integer quantity){
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }
}

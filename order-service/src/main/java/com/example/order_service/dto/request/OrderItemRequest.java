package com.example.order_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemRequest {
    @NotNull(message = "{NotNull}")
    private Long productVariantId;
    @NotNull(message = "{NotNull}")
    @Min(value = 1, message = "{Min}")
    private Integer quantity;

    @Builder
    private OrderItemRequest(Long productVariantId, Integer quantity){
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }
}

package com.example.order_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    @NotNull(message = "{NotNull}")
    private Long productVariantId;
    @NotNull(message = "{NotNull}")
    @Min(value = 1, message = "{Min}")
    private Integer quantity;
}

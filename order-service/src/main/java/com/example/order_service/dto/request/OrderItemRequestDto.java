package com.example.order_service.dto.request;

import jakarta.validation.constraints.Max;
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
public class OrderItemRequestDto {
    @NotNull(message = "Product Id is required")
    private Long productId;
    @NotNull
    @Min(value = 1, message = "Order Quantity must not be less 1")
    private int quantity;
}

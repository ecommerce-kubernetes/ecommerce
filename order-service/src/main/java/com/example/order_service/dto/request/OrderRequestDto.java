package com.example.order_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDto {
    @NotNull(message = "Order items is required")
    @Size(min = 1, max = 10, message = "items size must be between 1 and 10")
    @Valid
    private List<OrderItemRequestDto> items;
    @NotBlank(message = "Delivery Address is required")
    private String deliveryAddress;
}

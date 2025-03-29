package com.example.order_service.dto.request;

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
    private Long userId;
    private List<OrderItemRequestDto> items;
    private String deliveryAddress;
}

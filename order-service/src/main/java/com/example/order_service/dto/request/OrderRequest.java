package com.example.order_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    @NotEmpty(message = "{NotEmpty}")
    @Valid
    private List<OrderItemRequest> items;
    @NotBlank(message = "{NotBlank}")
    private String deliveryAddress;
    private Long couponId;
    private Integer useToCash;
    private Integer useToReserve;
}

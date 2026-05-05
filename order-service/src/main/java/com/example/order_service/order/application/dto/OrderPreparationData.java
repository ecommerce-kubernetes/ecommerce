package com.example.order_service.order.application.dto;

import com.example.order_service.order.application.dto.result.OrderProductResult;
import com.example.order_service.order.application.dto.result.OrderUserResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderPreparationData {
    private OrderUserResult.OrdererInfo user;
    private List<OrderProductResult.Info> products;
}

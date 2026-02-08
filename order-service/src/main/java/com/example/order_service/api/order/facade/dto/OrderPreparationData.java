package com.example.order_service.api.order.facade.dto;

import com.example.order_service.api.order.domain.service.dto.result.OrderProductInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderUserInfo;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderPreparationData {
    private OrderUserInfo user;
    private List<OrderProductInfo> products;
}

package com.example.order_service.api.order.application;

import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {
    public CreateOrderResponse createOrder(CreateOrderDto dto){

        return null;
    }
}

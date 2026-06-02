package com.example.order_service.order.application.saga.orchestrator;

import com.example.order_service.order.application.service.order.OrderCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderSagaManger {

    private final OrderCommandService orderCommandService;

    public void startSaga(String orderNo, String paymentKey) {
    }
}

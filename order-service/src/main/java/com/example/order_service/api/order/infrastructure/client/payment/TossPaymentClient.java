package com.example.order_service.api.order.infrastructure.client.payment;

import com.example.order_service.api.order.infrastructure.client.payment.dto.TossPaymentConfirmRequest;
import com.example.order_service.api.order.infrastructure.client.payment.dto.TossPaymentConfirmResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "tossPaymentClient",
        url = "https://api.tosspayments.com"
)
public interface TossPaymentClient {
    @PostMapping("/v1/payments/confirm")
    TossPaymentConfirmResponse confirmPayment(@RequestBody TossPaymentConfirmRequest request);
}

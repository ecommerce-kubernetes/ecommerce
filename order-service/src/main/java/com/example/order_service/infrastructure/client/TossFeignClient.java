package com.example.order_service.infrastructure.client;

import com.example.order_service.infrastructure.config.TossFeignConfig;
import com.example.order_service.infrastructure.dto.request.TossClientRequest;
import com.example.order_service.infrastructure.dto.request.TossConfirmRequest;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.infrastructure.dto.response.pg.TossConfirmResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "tossClient",
        url = "${payment.toss.url}",
        configuration = TossFeignConfig.class
)
public interface TossFeignClient {

    @PostMapping("/v1/payments/confirm")
    TossConfirmResponse confirmPayment(@RequestBody TossConfirmRequest request);

    @PostMapping("/v1/payments/{paymentKey}/cancel")
    TossClientResponse.Cancel cancelPayment(@PathVariable("paymentKey") String paymentKey,
                                            @RequestBody TossClientRequest.Cancel request);

    @GetMapping("/v1/payments/{paymentKey}")
    TossClientResponse.Inquiry inquirePayment(@PathVariable("paymentKey") String paymentKey);
}

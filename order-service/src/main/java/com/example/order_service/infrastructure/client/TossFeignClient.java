package com.example.order_service.infrastructure.client;

import com.example.order_service.infrastructure.config.TossFeignConfig;
import com.example.order_service.infrastructure.dto.request.TossCancelRequest;
import com.example.order_service.infrastructure.dto.request.TossConfirmRequest;
import com.example.order_service.infrastructure.dto.response.pg.TossCancelResponse;
import com.example.order_service.infrastructure.dto.response.pg.TossConfirmResponse;
import org.springframework.cloud.openfeign.FeignClient;
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
    TossCancelResponse cancelPayment(@PathVariable("paymentKey") String paymentKey,
                                     @RequestBody TossCancelRequest request);
}

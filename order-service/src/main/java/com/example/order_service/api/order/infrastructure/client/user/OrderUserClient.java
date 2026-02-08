package com.example.order_service.api.order.infrastructure.client.user;

import com.example.order_service.api.common.client.user.UserFeignConfig;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", contextId = "orderUserClient", configuration = UserFeignConfig.class)
public interface OrderUserClient {

    @GetMapping("/internal/users/{userId}/order-info")
    OrderUserResponse getOrderInfo(@PathVariable("userId") Long userId);
}

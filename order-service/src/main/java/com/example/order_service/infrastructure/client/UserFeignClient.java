package com.example.order_service.infrastructure.client;

import com.example.order_service.infrastructure.config.DefaultFeignConfig;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", contextId = "userClient", configuration = DefaultFeignConfig.class)
public interface UserFeignClient {

    @GetMapping("/internal/users/{userId}/order-info")
    UserClientResponse.UserInfo getUserInfoForOrder(@PathVariable("userId") Long userId);
}

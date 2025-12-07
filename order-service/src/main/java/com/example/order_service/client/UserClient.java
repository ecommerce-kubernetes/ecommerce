package com.example.order_service.client;

import com.example.order_service.api.common.config.FeignClientConfig;
import com.example.order_service.service.client.dto.UserBalanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "user-service", configuration = FeignClientConfig.class)
public interface UserClient {
    @GetMapping("/users/balance")
    UserBalanceResponse getUserBalance();
}

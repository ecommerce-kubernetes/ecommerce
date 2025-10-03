package com.example.order_service.client;

import com.example.order_service.config.FeignClientConfig;
import com.example.order_service.service.client.dto.UserBalanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", configuration = FeignClientConfig.class)
public interface UserClient {
    @GetMapping("/users/balance")
    UserBalanceResponse getUserBalance();
}

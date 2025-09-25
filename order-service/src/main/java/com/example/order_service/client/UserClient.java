package com.example.order_service.client;

import com.example.order_service.service.client.dto.UserBalanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/users/{userId}/balance")
    UserBalanceResponse getUserBalance(@PathVariable("userId") Long userId);
}

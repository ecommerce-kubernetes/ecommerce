package com.example.order_service.infrastructure.client;

import com.example.order_service.infrastructure.config.DefaultFeignConfig;
import com.example.order_service.infrastructure.dto.request.UserClientRequest;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", contextId = "userClient", configuration = DefaultFeignConfig.class)
public interface UserFeignClient {

    @GetMapping("/internal/users/{userId}/profile")
    UserClientResponse.Profile getUserProfile(@PathVariable("userId") Long userId);

    @GetMapping("/internal/users/{userId}/points")
    UserClientResponse.UserPoints getUserPoints(@PathVariable("userId") Long userId);

    @PostMapping("/internal/users/{userId}/points/validate-for-order")
    UserClientResponse.UserPoints getUserPointsForOrder(@PathVariable("userId") Long userId,
                                                        @RequestBody UserClientRequest.ValidatePoints request);
}

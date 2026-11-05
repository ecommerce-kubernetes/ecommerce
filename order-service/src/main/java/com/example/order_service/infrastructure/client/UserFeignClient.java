package com.example.order_service.infrastructure.client;

import com.example.order_service.infrastructure.config.DefaultFeignConfig;
import com.example.order_service.infrastructure.dto.response.user.UserPointsResponse;
import com.example.order_service.infrastructure.dto.response.user.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", contextId = "userClient", configuration = DefaultFeignConfig.class)
public interface UserFeignClient {

    @GetMapping("/internal/users/{userId}/profile")
    UserProfileResponse getUserProfile(@PathVariable("userId") Long userId);

    @GetMapping("/internal/users/{userId}/points")
    UserPointsResponse getUserPoints(@PathVariable("userId") Long userId);

}

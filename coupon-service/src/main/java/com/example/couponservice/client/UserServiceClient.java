package com.example.couponservice.client;

import com.example.couponservice.config.FeignClientConfig;
import com.example.couponservice.vo.ResponseUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", configuration = FeignClientConfig.class)
public interface UserServiceClient {

    @GetMapping("/users")
    ResponseUser getMyUserData(@RequestHeader("X-User-Id") Long userId);
}

package com.example.userservice.client;

import com.example.userservice.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "coupon-service", configuration = FeignClientConfig.class)
public interface CouponServiceClient {

    @RequestMapping(method = RequestMethod.PUT, value = "/coupon/change/phone-number")
    void changePhoneNumber(@RequestHeader("X-User-Id") Long userId, @RequestParam("phoneNumber") String phoneNumber);
}

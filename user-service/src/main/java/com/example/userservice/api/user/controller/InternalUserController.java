package com.example.userservice.api.user.controller;

import com.example.userservice.api.user.service.dto.result.UserOrderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    @GetMapping("/{userId}/order-info")
    public ResponseEntity<UserOrderResponse> getUserInfoForOrder(@PathVariable("userId") Long userId){
        return null;
    }
}

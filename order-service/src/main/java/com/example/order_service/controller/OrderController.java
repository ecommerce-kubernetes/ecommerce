package com.example.order_service.controller;

import com.example.order_service.dto.request.OrderRequestDto;
import com.example.order_service.dto.response.OrderResponseDto;
import com.example.order_service.service.JwtValidator;
import com.example.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final JwtValidator jwtValidator;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody @Validated OrderRequestDto orderRequestDto,
                                                        @RequestHeader("Authorization") String accessToken){

        String token = accessToken.replace("Bearer ", "");
        String userIdString = jwtValidator.getSub(token);
        Long userId = Long.parseLong(userIdString);
        OrderResponseDto orderResponseDto = orderService.saveOrder(userId, orderRequestDto);

        return ResponseEntity.status(HttpStatus.SC_CREATED).body(orderResponseDto);
    }
}

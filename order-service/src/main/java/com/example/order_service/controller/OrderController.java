package com.example.order_service.controller;

import com.example.order_service.common.SnowFlakeIdGenerator;
import com.example.order_service.dto.request.OrderRequestDto;
import com.example.order_service.dto.response.OrderResponseDto;
import com.example.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody @Validated OrderRequestDto orderRequestDto,
                                                        @RequestHeader("user-id") String userIdHeader){
        Long userId = Long.parseLong(userIdHeader);
        OrderResponseDto orderResponseDto = orderService.saveOrder(userId, orderRequestDto);

        return ResponseEntity.status(HttpStatus.SC_CREATED).body(orderResponseDto);
    }

}

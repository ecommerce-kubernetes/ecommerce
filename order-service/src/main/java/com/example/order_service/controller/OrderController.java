package com.example.order_service.controller;

import com.example.order_service.controller.util.specification.annotation.BadRequestApiResponse;
import com.example.order_service.controller.util.specification.annotation.NotFoundApiResponse;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.OrderResponseDto;
import com.example.order_service.dto.response.PageDto;
import com.example.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Order", description = "주문 관련 API")
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 생성")
    @NotFoundApiResponse @BadRequestApiResponse
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody @Validated OrderRequest orderRequest,
                                                        @RequestHeader("user-id") String userIdHeader){
        Long userId = Long.parseLong(userIdHeader);
        OrderResponseDto orderResponseDto = orderService.saveOrder(userId, orderRequest);

        return ResponseEntity.status(HttpStatus.SC_CREATED).body(orderResponseDto);
    }

    @Operation(summary = "주문 목록 조회")
    @BadRequestApiResponse
    @GetMapping("/{userId}")
    public ResponseEntity<PageDto<OrderResponseDto>> getAllOrdersByUserId(@PathVariable("userId") Long userId,
                                                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                                                  @RequestParam(value = "size", defaultValue = "10") int size,
                                                                  @RequestParam(value = "year",required = false) Integer year,
                                                                  @RequestParam(value = "keyword", required = false) String keyword){

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createAt");

        PageDto<OrderResponseDto> orderList = orderService.getOrderList(pageable, userId, year, keyword);
        return ResponseEntity.ok(orderList);
    }
}

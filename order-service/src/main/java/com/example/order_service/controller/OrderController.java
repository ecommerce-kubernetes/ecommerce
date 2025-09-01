package com.example.order_service.controller;

import com.example.order_service.controller.util.specification.annotation.BadRequestApiResponse;
import com.example.order_service.controller.util.specification.annotation.ConflictApiResponse;
import com.example.order_service.controller.util.specification.annotation.NotFoundApiResponse;
import com.example.order_service.controller.util.validator.PageableValidator;
import com.example.order_service.controller.util.validator.PageableValidatorFactory;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.dto.response.PageDto;
import com.example.order_service.entity.DomainType;
import com.example.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Order", description = "주문 관련 API")
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final PageableValidatorFactory factory;

    @Operation(summary = "주문 생성")
    @NotFoundApiResponse @BadRequestApiResponse @ConflictApiResponse
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Validated OrderRequest orderRequest,
                                                     @RequestHeader("X-User-Id") Long userId){
        OrderResponse orderResponse = orderService.saveOrder(userId, orderRequest);
        return ResponseEntity.status(HttpStatus.SC_CREATED).body(orderResponse);
    }

    @Operation(summary = "주문 목록 조회")
    @BadRequestApiResponse
    @GetMapping
    public ResponseEntity<PageDto<OrderResponse>> getOrders(@RequestHeader("X-User-Id") Long userId,
                                                                       @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
                                                                       @RequestParam(value = "year",required = false) String year,
                                                                       @RequestParam(value = "keyword", required = false) String keyword){
        PageableValidator validator = factory.getValidator(DomainType.ORDER);
        Pageable validatedPageable = validator.validate(pageable);
        PageDto<OrderResponse> orderList = orderService.getOrderList(validatedPageable, userId, year, keyword);
        return ResponseEntity.ok(orderList);
    }
}

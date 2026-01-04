package com.example.order_service.api.order.application.event;

import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderResultEvent {
    private Long orderId;
    private Long userId;
    private OrderEventStatus status;
    private String code;
    private String orderName;
    private Long finalPaymentAmount;
    private String message;

    @Builder
    private OrderResultEvent(Long orderId, Long userId, OrderEventStatus status, String code, String orderName,
                             Long finalPaymentAmount, String message) {
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.code = code;
        this.orderName = orderName;
        this.finalPaymentAmount = finalPaymentAmount;
        this.message = message;
    }

    public static OrderResultEvent of(Long orderId, Long userId, OrderEventStatus status, String code,
                                      String orderName, Long finalPaymentAmount, String message) {
        return OrderResultEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .status(status)
                .code(code)
                .orderName(orderName)
                .finalPaymentAmount(finalPaymentAmount)
                .message(message)
                .build();
    }

    public static OrderResultEvent failure(OrderDto orderDto) {
        return OrderResultEvent.builder()
                .orderId(orderDto.getOrderId())
                .userId(orderDto.getUserId())
                .status(OrderEventStatus.FAILURE)
                .code(orderDto.getOrderFailureCode().name())
                .orderName(orderDto.getOrderName())
                .finalPaymentAmount(orderDto.getOrderPriceInfo().getFinalPaymentAmount())
                .message(orderDto.getOrderFailureCode().getName())
                .build();
    }

    public static OrderResultEvent paymentReady(OrderDto orderDto) {
        return OrderResultEvent.builder()
                .orderId(orderDto.getOrderId())
                .userId(orderDto.getUserId())
                .status(OrderEventStatus.SUCCESS)
                .code("PAYMENT_READY")
                .orderName(orderDto.getOrderName())
                .finalPaymentAmount(orderDto.getOrderPriceInfo().getFinalPaymentAmount())
                .message("결제 준비 완료")
                .build();
    }
}

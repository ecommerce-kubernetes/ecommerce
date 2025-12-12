package com.example.order_service.api.order.application.dto.result;

import com.example.order_service.api.order.domain.model.Orders;
import com.example.order_service.api.order.domain.service.dto.result.OrderCreationResult;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateOrderResponse {
    private Long orderId;
    private String status;
    private String message;
    private Integer totalQuantity;
    private Long finalPaymentAmount;
    private LocalDateTime createAt;

    @Builder
    private CreateOrderResponse(Long orderId, String status, String message, Integer totalQuantity, LocalDateTime createAt, Long finalPaymentAmount){
        this.orderId = orderId;
        this.status = status;
        this.message = message;
        this.totalQuantity = totalQuantity;
        this.finalPaymentAmount = finalPaymentAmount;
        this.createAt = createAt;
    }

    //TODO 리팩터링을 위해 임시 생성이므로 제거
    public static CreateOrderResponse of(Orders order, String url){
        return null;
    }

    public static CreateOrderResponse of(OrderCreationResult result) {
        return CreateOrderResponse.builder()
                .orderId(result.getOrderId())
                .status(result.getStatus())
                .message("주문 요청이 접수되었습니다")
                .finalPaymentAmount(result.getPaymentInfo().getFinalPaymentAmount())
                .createAt(result.getOrderedAt())
                .build();
    }
}

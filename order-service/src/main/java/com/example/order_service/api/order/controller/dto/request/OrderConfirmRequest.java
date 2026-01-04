package com.example.order_service.api.order.controller.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderConfirmRequest {
    @NotNull(message = "주문 번호는 필수 입니다")
    private String orderNo;
    @NotEmpty(message = "결제 키는 필수 입니다")
    private String paymentKey;
    @NotNull(message = "결제 가격은 필수 입니다")
    private Long amount;

    @Builder
    private OrderConfirmRequest(String orderNo, String paymentKey, Long amount) {
        this.orderNo = orderNo;
        this.paymentKey = paymentKey;
        this.amount = amount;
    }
}

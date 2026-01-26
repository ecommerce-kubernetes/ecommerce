package com.example.order_service.api.order.domain.service.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderPaymentInfo {
    private String orderNo;
    private String paymentKey;
    private Long totalAmount;
    private String status;
    private String method;
    private String approvedAt;
}

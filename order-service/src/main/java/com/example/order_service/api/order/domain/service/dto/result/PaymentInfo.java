package com.example.order_service.api.order.domain.service.dto.result;

import lombok.Getter;

@Getter
public class PaymentInfo {
    private long totalOriginPrice;
    private long totalProductDiscount;
    private long couponDiscount;
    private long usedPoint;
    private long finalPaymentAmount;
}

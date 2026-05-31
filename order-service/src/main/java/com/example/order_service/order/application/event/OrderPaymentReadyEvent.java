package com.example.order_service.order.application.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderPaymentReadyEvent {
    private String orderNo;
    private Long userId;
    private String code;
    private String orderName;
    private Long finalPaymentAmount;


}

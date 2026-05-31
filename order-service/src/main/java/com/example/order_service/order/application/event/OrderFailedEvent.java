package com.example.order_service.order.application.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderFailedEvent {
    private String orderNo;
    private Long userId;
    private String code;
    private String orderName;

}

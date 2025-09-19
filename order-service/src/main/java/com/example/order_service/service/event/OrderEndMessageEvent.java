package com.example.order_service.service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderEndMessageEvent extends ApplicationEvent {
    private Long orderId;
    private String status;
    public OrderEndMessageEvent(Object source, Long orderId, String status){
        super(source);
        this.orderId = orderId;
        this.status = status;
    }
}

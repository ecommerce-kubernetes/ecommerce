package com.example.order_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KafkaOrderStatusDto {
    private Long orderId;
    private String status;
    private String message;
}

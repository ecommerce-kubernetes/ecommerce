package com.example.order_service.api.order.controller.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderSearchCondition {
    private Integer page;
    private Integer size;
    private String sort;
    private String direction;
    private String year;
    private String productName;
}

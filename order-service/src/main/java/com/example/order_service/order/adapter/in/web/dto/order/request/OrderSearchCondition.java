package com.example.order_service.order.adapter.in.web.dto.order.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Year;

@Getter
@Setter
@NoArgsConstructor
public class OrderSearchCondition {
    private String sort = "latest";
    private Year year;
    private String productName;
}

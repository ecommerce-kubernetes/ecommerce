package com.example.order_service.order.api.web.dto.order.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderSearchCondition {
    private String sort = "latest";
    private String year;
    private String productName;
}

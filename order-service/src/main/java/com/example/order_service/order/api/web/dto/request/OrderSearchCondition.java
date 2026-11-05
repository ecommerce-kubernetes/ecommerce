package com.example.order_service.order.api.web.dto.request;

import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
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

    public OrderSearchCommand toCommand() {
        return OrderSearchCommand.of(sort, year, productName);
    }
}

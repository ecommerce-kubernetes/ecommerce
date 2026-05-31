package com.example.order_service.order.application.service.order.dto.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderSearchCommand {
    String sort;
    String year;
    String productName;

    public static OrderSearchCommand of(String sort, String year, String productName) {
        return OrderSearchCommand.builder()
                .sort(sort)
                .year(year)
                .productName(productName)
                .build();
    }
}

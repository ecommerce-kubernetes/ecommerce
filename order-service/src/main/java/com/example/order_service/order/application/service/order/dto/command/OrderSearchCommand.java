package com.example.order_service.order.application.service.order.dto.command;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

@Getter
@Builder
public class OrderSearchCommand {
    String sort;
    String year;
    String productName;
    Pageable pageable;

    public static OrderSearchCommand of(String sort, String year, String productName, Pageable pageable) {
        return OrderSearchCommand.builder()
                .sort(sort)
                .year(year)
                .productName(productName)
                .pageable(pageable)
                .build();
    }
}

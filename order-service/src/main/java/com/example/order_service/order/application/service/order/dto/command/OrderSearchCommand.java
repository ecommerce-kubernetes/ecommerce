package com.example.order_service.order.application.service.order.dto.command;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

import java.time.Year;

@Getter
@Builder
public class OrderSearchCommand {
    private OrderSortType sort;
    private Year year;
    private String productName;
    private Pageable pageable;

    public static OrderSearchCommand of(String sort, Year year, String productName, Pageable pageable) {
        OrderSortType orderSortType = (sort == null) ? OrderSortType.LATEST :
                switch (sort.toLowerCase()) {
                    case "latest" -> OrderSortType.LATEST;
                    case "oldest" -> OrderSortType.OLDEST;
                    default -> OrderSortType.LATEST;
                };

        return OrderSearchCommand.builder()
                .sort(orderSortType)
                .year(year)
                .productName(productName)
                .pageable(pageable)
                .build();
    }
}

package com.example.order_service.order.api.dto.request;

import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

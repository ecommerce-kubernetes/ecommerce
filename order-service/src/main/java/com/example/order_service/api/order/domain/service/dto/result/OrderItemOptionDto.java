package com.example.order_service.api.order.domain.service.dto.result;

import com.example.order_service.api.order.domain.model.OrderItemOption;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItemOptionDto {
    private String optionTypeName;
    private String optionValueName;

    public static OrderItemOptionDto from(OrderItemOption orderItemOption) {
        return OrderItemOptionDto.builder()
                .optionTypeName(orderItemOption.getOptionTypeName())
                .optionValueName(orderItemOption.getOptionValueName())
                .build();
    }
}

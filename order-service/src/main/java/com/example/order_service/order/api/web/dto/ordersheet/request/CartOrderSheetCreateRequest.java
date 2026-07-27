package com.example.order_service.order.api.web.dto.ordersheet.request;

import com.example.order_service.order.application.service.ordersheet.dto.command.CreateCartOrderSheetCommand;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record CartOrderSheetCreateRequest(
        @NotEmpty(message = "{orderSheet.cartItems.notEmpty}")
        List<Long> cartItemIds
) {

    public CreateCartOrderSheetCommand toCommand(Long userId) {
        return CreateCartOrderSheetCommand.builder()
                .userId(userId)
                .cartItemIds(cartItemIds)
                .build();
    }
}

package com.example.order_service.order.api.web.dto.ordersheet.request;

import com.example.order_service.order.application.service.ordersheet.dto.command.CreateDirectOrderSheetCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record DirectOrderSheetCreateRequest(
        @Valid
        @NotEmpty(message = "{orderSheet.items.notEmpty}")
        List<OrderVariant> items
) {

    @Builder
    public record OrderVariant(
            @NotNull(message = "{orderSheet.item.productVariantId.notNull}")
            Long productVariantId,
            @NotNull(message = "{orderSheet.item.quantity.notNull}")
            @Min(value = 1, message = "{orderSheet.item.quantity.min}")
            Integer quantity
    ) {
        public CreateDirectOrderSheetCommand.OrderVariant toCommand() {
            return CreateDirectOrderSheetCommand.OrderVariant
                    .builder()
                    .productVariantId(productVariantId)
                    .quantity(quantity)
                    .build();
        }
    }

    public CreateDirectOrderSheetCommand toCommand(Long userId) {
        return CreateDirectOrderSheetCommand.builder()
                .userId(userId)
                .items(mapToCommandOrderVariants())
                .build();
    }

    private List<CreateDirectOrderSheetCommand.OrderVariant> mapToCommandOrderVariants() {
        return this.items.stream().map(OrderVariant::toCommand).toList();
    }
}

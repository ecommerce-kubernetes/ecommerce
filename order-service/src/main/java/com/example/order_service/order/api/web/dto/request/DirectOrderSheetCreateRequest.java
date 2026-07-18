package com.example.order_service.order.api.web.dto.request;

import com.example.order_service.order.application.service.ordersheet.dto.command.CreateDirectOrderSheetCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record DirectOrderSheetCreateRequest(
        @Valid
        @NotEmpty(message = "{orderSheet.variants.notEmpty}")
        List<OrderVariant> variants
) {

    @Builder
    public record OrderVariant(
            @NotNull(message = "{orderSheet.variant.productVariantId.notNull}")
            Long productVariantId,
            @NotNull(message = "{orderSheet.variant.quantity.notNull}")
            @Min(value = 1, message = "{orderSheet.variant.quantity.min}")
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
                .variants(mapToCommandOrderVariants())
                .build();
    }

    private List<CreateDirectOrderSheetCommand.OrderVariant> mapToCommandOrderVariants() {
        return this.variants.stream().map(OrderVariant::toCommand).toList();
    }
}

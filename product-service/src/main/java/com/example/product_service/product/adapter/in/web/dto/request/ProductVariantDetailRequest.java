package com.example.product_service.product.adapter.in.web.dto.request;

import com.example.product_service.product.application.service.dto.command.AddProductVariantCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

@Builder
public record ProductVariantDetailRequest(
        @Min(value = 100, message = "{product.variant.originalPrice.min}")
        Long originalPrice,
        @Min(value = 0, message = "{product.variant.discountRate.min}")
        @Max(value = 100, message = "{product.variant.discountRate.max}")
        Integer discountRate,
        @Min(value = 1, message = "{product.variant.stockQuantity.min}")
        Integer stockQuantity,
        @UniqueElements(message = "{product.variant.optionValueIds.unique}")
        List<Long> optionValueIds
) {
    public AddProductVariantCommand.VariantDetail toCommand() {
        return AddProductVariantCommand.VariantDetail.builder()
                .originalPrice(originalPrice)
                .discountRate(discountRate)
                .stockQuantity(stockQuantity)
                .optionValueIds(optionValueIds)
                .build();
    }
}

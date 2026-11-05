package com.example.product_service.product.adapter.in.web.dto.request;

import com.example.product_service.product.application.service.dto.command.AddProductVariantCommand;
import jakarta.validation.Valid;
import lombok.Builder;

import java.util.List;

@Builder
public record AddProductVariantRequest(
        @Valid
        List<ProductVariantDetailRequest> variants
) {
    public AddProductVariantCommand toCommand(Long productId) {
        List<AddProductVariantCommand.VariantDetail> variantDetails = variants.stream()
                .map(ProductVariantDetailRequest::toCommand)
                .toList();
        return AddProductVariantCommand.builder()
                .productId(productId)
                .variants(variantDetails)
                .build();
    }
}

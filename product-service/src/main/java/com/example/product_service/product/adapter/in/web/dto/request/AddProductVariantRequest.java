package com.example.product_service.product.adapter.in.web.dto.request;

import com.example.product_service.product.application.service.dto.command.ProductCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record AddProductVariantRequest(
        @Valid
        @NotEmpty(message = "상품 변형 리스트는 필수입니다")
        List<ProductVariantDetailRequest> variants
) {
    public ProductCommand.AddVariant toCommand(Long productId) {
        List<ProductCommand.VariantDetail> variantDetails = mappingVariantDetails(variants);
        return ProductCommand.AddVariant.builder()
                .productId(productId)
                .variants(variantDetails)
                .build();
    }

    private List<ProductCommand.VariantDetail> mappingVariantDetails(List<ProductVariantDetailRequest> variants) {
        return variants.stream().map(
                v -> ProductCommand.VariantDetail.builder()
                        .originalPrice(v.originalPrice())
                        .discountRate(v.discountRate())
                        .stockQuantity(v.stockQuantity())
                        .optionValueIds(v.optionValueIds())
                        .build()).toList();
    }
}

package com.example.product_service.product.domain.context;

import com.example.product_service.product.domain.vo.SalePrice;
import lombok.Builder;

import java.util.List;

@Builder
public record AddVariantContext(
        Long id,
        String sku,
        SalePrice salePrice,
        int stock,
        List<AddVariantOptionValueContext> optionValues
) {

    @Builder
    public record AddVariantOptionValueContext(
            Long id,
            Long optionTypeId,
            Long optionValueId
    ) {}
}

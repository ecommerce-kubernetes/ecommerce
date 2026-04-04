package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.ProductVariant;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class AddVariantResult {
    private Long productId;
    private List<VariantResult> variants;

    @Builder
    private AddVariantResult(Long productId, List<VariantResult> variants) {
        this.productId = productId;
        this.variants = variants;
    }

    public static AddVariantResult of(Long productId, List<ProductVariant> variants) {
        List<VariantResult> variantResult = variants.stream().map(VariantResult::from).toList();
        return AddVariantResult.builder()
                .productId(productId)
                .variants(variantResult)
                .build();
    }
}

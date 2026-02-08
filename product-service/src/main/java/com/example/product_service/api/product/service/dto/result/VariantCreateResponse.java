package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.ProductVariant;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class VariantCreateResponse {
    private Long productId;
    private List<VariantResponse> variants;

    @Builder
    private VariantCreateResponse(Long productId, List<VariantResponse> variants) {
        this.productId = productId;
        this.variants = variants;
    }

    public static VariantCreateResponse of(Long productId, List<ProductVariant> variants) {
        List<VariantResponse> variantResponse = variants.stream().map(VariantResponse::from).toList();
        return VariantCreateResponse.builder()
                .productId(productId)
                .variants(variantResponse)
                .build();
    }
}

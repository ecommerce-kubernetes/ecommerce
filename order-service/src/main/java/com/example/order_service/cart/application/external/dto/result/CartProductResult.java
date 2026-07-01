package com.example.order_service.cart.application.external.dto.result;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

import java.util.List;

public class CartProductResult {

    @Builder
    public record Info(
            Long productId,
            Long productVariantId,
            CartProductStatus status,
            String sku,
            String productName,
            Integer stock,
            Money originalPrice,
            Integer discountRate,
            Money discountAmount,
            Money discountedPrice,
            String thumbnail,
            List<ProductOption> options
    ) {
    }

    @Builder
    public record ProductOption(
            String optionTypeName,
            String optionValueName
    ) {
    }
}

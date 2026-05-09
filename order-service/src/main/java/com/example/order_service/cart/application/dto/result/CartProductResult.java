package com.example.order_service.cart.application.dto.result;

import com.example.order_service.cart.domain.model.vo.ProductStatus;
import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

import java.util.List;

public class CartProductResult {

    @Builder
    public record Info(
            Long productId,
            Long productVariantId,
            ProductStatus status,
            String sku,
            String productName,
            Integer stock,
            Money originalPrice,
            Integer discountRate,
            Money discountAmount,
            Money discountedPrice,
            String thumbnail,
            List<Option> options
    ) {
    }

    @Builder
    public record Option(
            String optionTypeName,
            String optionValueName
    ) {
    }
}

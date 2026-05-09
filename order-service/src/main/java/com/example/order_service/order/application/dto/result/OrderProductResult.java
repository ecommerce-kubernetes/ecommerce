package com.example.order_service.order.application.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.model.vo.ProductStatus;
import lombok.Builder;

import java.util.List;

public class OrderProductResult {

    @Builder
    public record Info(
            Long productId,
            String productName,
            Long productVariantId,
            ProductStatus status,
            String sku,
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

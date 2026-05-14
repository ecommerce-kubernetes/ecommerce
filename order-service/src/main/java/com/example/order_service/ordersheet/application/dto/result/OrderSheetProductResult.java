package com.example.order_service.ordersheet.application.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.ordersheet.domain.model.vo.ProductStatus;
import lombok.Builder;

import java.util.List;
import java.util.Map;

public class OrderSheetProductResult {

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

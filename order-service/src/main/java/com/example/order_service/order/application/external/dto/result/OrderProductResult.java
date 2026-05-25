package com.example.order_service.order.application.external.dto.result;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

import java.util.List;

public class OrderProductResult {

    @Builder
    public record ProductList(
            List<Info> products
    ){}

    @Builder
    public record Info(
            Long productId,
            Long productVariantId,
            String sku,
            String productName,
            Money originalPrice,
            Integer discountRate,
            Money discountAmount,
            Money discountedPrice,
            String thumbnail,
            List<Option> options
    ) {}

    @Builder
    public record Option(
            String optionTypeName,
            String optionValueName
    ) {}
}

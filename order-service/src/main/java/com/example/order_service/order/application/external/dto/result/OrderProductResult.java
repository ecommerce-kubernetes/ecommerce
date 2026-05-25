package com.example.order_service.order.application.external.dto.result;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OrderProductResult {

    @Builder
    public record ProductList(
            List<Info> products
    ){
        public Map<Long, OrderProductResult.Info> getProductsMap() {
            return products.stream()
                    .collect(Collectors.toMap(OrderProductResult.Info::productVariantId, Function.identity()));
        }
    }

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

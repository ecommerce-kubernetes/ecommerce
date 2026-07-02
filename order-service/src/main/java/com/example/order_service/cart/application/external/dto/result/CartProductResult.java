package com.example.order_service.cart.application.external.dto.result;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CartProductResult {

    @Builder
    public record ProductList(
            List<Info> products
    ) {
        public Map<Long, Info> getProductsMap() {
            return products.stream()
                    .collect(Collectors.toMap(Info::productVariantId, Function.identity()));
        }
    }

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

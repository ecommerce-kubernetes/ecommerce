package com.example.order_service.api.order.domain.service.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderItemCreationContext {
    private ProductSpec productSpec;
    private PriceSpec priceSpec;
    private int quantity;
    private Long lineTotal;
    private List<CreateItemOptionSpec> itemOptionSpecs;

    public static OrderItemCreationContext of(ProductSpec productSpec, PriceSpec priceSpec, int quantity, Long lineTotal,
                                              List<CreateItemOptionSpec> itemOptionSpecs) {
        return OrderItemCreationContext.builder()
                .productSpec(productSpec)
                .priceSpec(priceSpec)
                .quantity(quantity)
                .lineTotal(lineTotal)
                .itemOptionSpecs(itemOptionSpecs)
                .build();
    }

    @Builder
    @Getter
    public static class CreateItemOptionSpec {
        private String optionTypeName;
        private String optionValueName;

        public static CreateItemOptionSpec of(String optionTypeName, String optionValueName) {
            return CreateItemOptionSpec.builder()
                    .optionTypeName(optionTypeName)
                    .optionValueName(optionValueName)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ProductSpec {
        private Long productId;
        private Long productVariantId;
        private String sku;
        private String productName;
        private String thumbnail;

        public static ProductSpec of(Long productId, Long productVariantId, String sku, String productName, String thumbnail) {
            return ProductSpec.builder()
                    .productId(productId)
                    .productVariantId(productVariantId)
                    .sku(sku)
                    .productName(productName)
                    .thumbnail(thumbnail)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class PriceSpec {
        private Long originPrice;
        private Integer discountRate;
        private Long discountAmount;
        private Long discountedPrice;

        public static PriceSpec of(Long originPrice, Integer discountRate, Long discountAmount, Long discountedPrice) {
            return PriceSpec.builder()
                    .originPrice(originPrice)
                    .discountRate(discountRate)
                    .discountAmount(discountAmount)
                    .discountedPrice(discountedPrice)
                    .build();
        }
    }
}

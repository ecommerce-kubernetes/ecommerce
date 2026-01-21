package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.option.domain.model.OptionValue;
import com.example.product_service.api.product.domain.model.ProductStatus;
import com.example.product_service.api.product.domain.model.ProductVariant;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InternalVariantResponse {
    private Long productId;
    private Long productVariantId;
    private ProductStatus status;
    private String sku;
    private String productName;
    private UnitPrice unitPrice;
    private Integer stockQuantity;
    private String thumbnailUrl;
    private List<ItemOption> itemOptions;

    @Getter
    @Builder
    public static class UnitPrice {
        private long originalPrice;
        private int discountRate;
        private long discountAmount;
        private long discountedPrice;
    }

    @Getter
    @Builder
    public static class ItemOption {
        private String optionTypeName;
        private String optionValueName;

        public static ItemOption from(OptionValue optionValue) {
            return ItemOption.builder()
                    .optionTypeName(optionValue.getOptionType().getName())
                    .optionValueName(optionValue.getName())
                    .build();
        }
    }

    public static InternalVariantResponse from(ProductVariant variant) {
        List<ItemOption> options = variant.getProductVariantOptions().stream()
                .map(v -> ItemOption.from(v.getOptionValue())).toList();
        return InternalVariantResponse.builder()
                .productId(variant.getProduct().getId())
                .productVariantId(variant.getId())
                .status(variant.getProduct().getStatus())
                .sku(variant.getSku())
                .productName(variant.getProduct().getName())
                .unitPrice(
                        UnitPrice.builder()
                                .originalPrice(variant.getOriginalPrice())
                                .discountRate(variant.getDiscountRate())
                                .discountAmount(variant.getDiscountAmount())
                                .discountedPrice(variant.getPrice())
                                .build())
                .stockQuantity(variant.getStockQuantity())
                .thumbnailUrl(variant.getProduct().getThumbnail())
                .itemOptions(options)
                .build();
    }
}

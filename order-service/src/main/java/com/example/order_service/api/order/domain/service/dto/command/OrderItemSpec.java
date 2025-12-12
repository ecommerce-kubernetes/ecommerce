package com.example.order_service.api.order.domain.service.dto.command;

import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderItemSpec {
    private Long productId;
    private Long productVariantId;
    private String productName;
    private String thumbnailUrl;
    private UnitPrice unitPrice;
    private int quantity;
    private Long lineTotal;
    private List<ItemOption> itemOptions;

    @Builder
    private OrderItemSpec(Long productId, Long productVariantId, String productName, String thumbnailUrl,
                          UnitPrice unitPrice, int quantity, Long lineTotal, List<ItemOption> itemOptions){
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.thumbnailUrl = thumbnailUrl;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
        this.itemOptions = itemOptions;
    }

    public static OrderItemSpec of(OrderProductResponse product, int quantity) {
        UnitPrice price = UnitPrice.builder()
                .originalPrice(product.getUnitPrice().getOriginalPrice())
                .discountRate(product.getUnitPrice().getDiscountRate())
                .discountAmount(product.getUnitPrice().getDiscountAmount())
                .discountedPrice(product.getUnitPrice().getDiscountedPrice())
                .build();

        List<ItemOption> options = product.getItemOptions().stream().map(item -> ItemOption.builder()
                .optionTypeName(item.getOptionTypeName())
                .optionValueName(item.getOptionValueName())
                .build()).toList();

        return of(
                product.getProductId(),
                product.getProductVariantId(),
                product.getProductName(),
                product.getThumbnailUrl(),
                price,
                quantity,
                product.getUnitPrice().getDiscountedPrice() * quantity,
                options
        );
    }

    public static OrderItemSpec of(Long productId, Long productVariantId, String productName, String thumbnailUrl,
                                   UnitPrice unitPrice, int quantity, long lineTotal, List<ItemOption> itemOptions){
        return OrderItemSpec
                .builder()
                .productId(productId)
                .productVariantId(productVariantId)
                .productName(productName)
                .thumbnailUrl(thumbnailUrl)
                .unitPrice(unitPrice)
                .quantity(quantity)
                .lineTotal(lineTotal)
                .itemOptions(itemOptions)
                .build();
    }

    @Builder
    @Getter
    public static class UnitPrice {
        private long originalPrice;
        private int discountRate;
        private long discountAmount;
        private long discountedPrice;
    }

    @Builder
    @Getter
    public static class ItemOption {
        private String optionTypeName;
        private String optionValueName;
    }
}

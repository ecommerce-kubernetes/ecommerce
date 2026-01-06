package com.example.order_service.api.order.domain.service.dto.result;

import com.example.order_service.api.order.domain.model.ItemOption;
import com.example.order_service.api.order.domain.model.OrderItem;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderItemDto {
    private Long orderItemId;
    private Long productId;
    private Long productVariantId;
    private String productName;
    private String thumbnailUrl;
    private int quantity;
    private Long lineTotal;
    private UnitPrice unitPrice;
    private List<ItemOptionDto> itemOptionDtos;

    @Builder
    private OrderItemDto(Long orderItemId, Long productId, Long productVariantId, String productName, String thumbnailUrl, int quantity,
                         Long lineTotal, UnitPrice unitPrice, List<ItemOptionDto> itemOptionDtos){
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.thumbnailUrl = thumbnailUrl;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
        this.unitPrice = unitPrice;
        this.itemOptionDtos = itemOptionDtos;
    }

    public static OrderItemDto from(OrderItem orderItem){
        return of(orderItem.getId(), orderItem.getProductId(), orderItem.getProductVariantId(), orderItem.getProductName(),
                orderItem.getThumbnail(), orderItem.getQuantity(), orderItem.getLineTotal(),
                UnitPrice.from(orderItem),
                mappingItemOptions(orderItem)
                );
    }

    public static OrderItemDto of(Long orderItemId, Long productId, Long productVariantId, String productName, String thumbnailUrl, int quantity,
                                  Long lineTotal, UnitPrice unitPrice, List<ItemOptionDto> itemOptionDtos) {
        return OrderItemDto.builder()
                .orderItemId(orderItemId)
                .productId(productId)
                .productVariantId(productVariantId)
                .productName(productName)
                .thumbnailUrl(thumbnailUrl)
                .quantity(quantity)
                .lineTotal(lineTotal)
                .unitPrice(unitPrice)
                .itemOptionDtos(itemOptionDtos)
                .build();
    }

    private static List<ItemOptionDto> mappingItemOptions(OrderItem orderItem){
        return orderItem.getItemOptions().stream().map(ItemOptionDto::from).toList();
    }

    @Builder
    @Getter
    public static class UnitPrice {
        private long originalPrice;
        private int discountRate;
        private long discountAmount;
        private long discountedPrice;

        private static UnitPrice from(OrderItem orderItem){
            return UnitPrice.builder()
                    .originalPrice(orderItem.getOriginPrice())
                    .discountRate(orderItem.getDiscountRate())
                    .discountAmount(orderItem.getDiscountAmount())
                    .discountedPrice(orderItem.getDiscountedPrice())
                    .build();
        }
    }

    @Builder
    @Getter
    public static class ItemOptionDto {
        private String optionTypeName;
        private String optionValueName;

        private static ItemOptionDto from(ItemOption itemOption){
            return ItemOptionDto.builder()
                    .optionTypeName(itemOption.getOptionTypeName())
                    .optionValueName(itemOption.getOptionValueName())
                    .build();
        }
    }
}

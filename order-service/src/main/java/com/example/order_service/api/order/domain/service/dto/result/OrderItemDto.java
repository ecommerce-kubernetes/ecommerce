package com.example.order_service.api.order.domain.service.dto.result;

import com.example.order_service.api.order.domain.model.OrderItem;
import com.example.order_service.api.order.domain.model.OrderItemOption;
import com.example.order_service.api.order.domain.model.vo.OrderItemPrice;
import com.example.order_service.api.order.domain.model.vo.OrderedProduct;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderItemDto {
    private Long id;
    private OrderedProductInfo orderedProduct;
    private OrderItemPriceInfo orderItemPrice;
    private int quantity;
    private Long lineTotal;
    private List<OrderItemOptionDto> itemOptions;

    public static OrderItemDto from(OrderItem orderItem) {
        List<OrderItemOptionDto> itemOptions = orderItem.getOrderItemOptions().stream().map(OrderItemOptionDto::from).toList();
        return OrderItemDto.builder()
                .id(orderItem.getId())
                .orderedProduct(OrderedProductInfo.from(orderItem.getOrderedProduct()))
                .orderItemPrice(OrderItemPriceInfo.from(orderItem.getOrderItemPrice()))
                .quantity(orderItem.getQuantity())
                .lineTotal(orderItem.getLineTotal())
                .itemOptions(itemOptions)
                .build();
    }

    @Getter
    @Builder
    public static class OrderedProductInfo {
        private Long productId;
        private Long productVariantId;
        private String sku;
        private String productName;
        private String thumbnail;

        public static OrderedProductInfo from(OrderedProduct product) {
            return OrderedProductInfo.builder()
                    .productId(product.getProductId())
                    .productVariantId(product.getProductVariantId())
                    .sku(product.getSku())
                    .productName(product.getProductName())
                    .thumbnail(product.getThumbnail())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OrderItemPriceInfo {
        private Long originPrice;
        private Integer discountRate;
        private Long discountAmount;
        private Long discountedPrice;

        public static OrderItemPriceInfo from(OrderItemPrice orderItemPrice) {
            return OrderItemPriceInfo.builder()
                    .originPrice(orderItemPrice.getOriginPrice())
                    .discountRate(orderItemPrice.getDiscountRate())
                    .discountAmount(orderItemPrice.getDiscountAmount())
                    .discountedPrice(orderItemPrice.getDiscountedPrice())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OrderItemOptionDto {
        private String optionTypeName;
        private String optionValueName;

        public static OrderItemOptionDto from(OrderItemOption orderItemOption) {
            return OrderItemOptionDto.builder()
                    .optionTypeName(orderItemOption.getOptionTypeName())
                    .optionValueName(orderItemOption.getOptionValueName())
                    .build();
        }
    }

}

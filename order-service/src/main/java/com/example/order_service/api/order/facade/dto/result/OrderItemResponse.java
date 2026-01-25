package com.example.order_service.api.order.facade.dto.result;

import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto.OrderItemOptionDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto.OrderItemPriceInfo;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderItemResponse {
    private Long productId;
    private Long productVariantId;
    private String productName;
    private String thumbnailUrl;
    private int quantity;
    private OrderItemPriceResponse unitPrice;
    private Long lineTotal;
    private List<OrderItemOptionResponse> options;

    public static OrderItemResponse from(OrderItemDto orderItemDto) {
        List<OrderItemOptionResponse> options = orderItemDto.getItemOptions().stream().map(OrderItemOptionResponse::from).toList();
        return OrderItemResponse.builder()
                .productId(orderItemDto.getOrderedProduct().getProductId())
                .productVariantId(orderItemDto.getOrderedProduct().getProductVariantId())
                .productName(orderItemDto.getOrderedProduct().getProductName())
                .thumbnailUrl(orderItemDto.getOrderedProduct().getThumbnail())
                .quantity(orderItemDto.getQuantity())
                .unitPrice(OrderItemPriceResponse.from(orderItemDto.getOrderItemPrice()))
                .lineTotal(orderItemDto.getLineTotal())
                .options(options)
                .build();
    }

    @Getter
    @Builder
    public static class OrderItemPriceResponse {
        private long originalPrice;
        private int discountRate;
        private long discountAmount;
        private long discountedPrice;

        private static OrderItemPriceResponse from(OrderItemPriceInfo orderItemPriceInfo) {
            return OrderItemPriceResponse.builder()
                    .originalPrice(orderItemPriceInfo.getOriginPrice())
                    .discountRate(orderItemPriceInfo.getDiscountRate())
                    .discountAmount(orderItemPriceInfo.getDiscountAmount())
                    .discountedPrice(orderItemPriceInfo.getDiscountedPrice())
                    .build();
        }
    }
    @Getter
    @Builder
    public static class OrderItemOptionResponse {
        private String optionTypeName;
        private String optionValueName;

        private static OrderItemOptionResponse from(OrderItemOptionDto optionDto) {
            return OrderItemOptionResponse.builder()
                    .optionTypeName(optionDto.getOptionTypeName())
                    .optionValueName(optionDto.getOptionValueName())
                    .build();
        }
    }
}

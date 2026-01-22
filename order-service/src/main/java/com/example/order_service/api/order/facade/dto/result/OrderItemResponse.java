package com.example.order_service.api.order.facade.dto.result;

import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemResponse {
    private Long productId;
    private Long productVariantId;
    private String productName;
    private String thumbNailUrl;
    private int quantity;
    private OrderItemPrice unitPrice;
    private Long lineTotal;
    private List<OrderItemOption> options;

    @Builder
    private OrderItemResponse(Long productId, Long productVariantId, String productName, String thumbNailUrl, int quantity,
                              OrderItemPrice unitPrice, Long lineTotal, List<OrderItemOption> options){
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.thumbNailUrl = thumbNailUrl;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        this.options = options;
    }

    @Getter
    @Builder
    public static class OrderItemPrice {
        private long originalPrice;
        private int discountRate;
        private long discountAmount;
        private long discountedPrice;

        public static OrderItemPrice from(OrderItemDto.UnitPrice price) {
            return OrderItemPrice.builder()
                    .originalPrice(price.getOriginalPrice())
                    .discountRate(price.getDiscountRate())
                    .discountAmount(price.getDiscountAmount())
                    .discountedPrice(price.getDiscountedPrice())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OrderItemOption {
        private String optionTypeName;
        private String optionValueName;

        public static OrderItemOption from(OrderItemDto.ItemOptionDto option) {
            return OrderItemOption.builder()
                    .optionTypeName(option.getOptionTypeName())
                    .optionValueName(option.getOptionValueName())
                    .build();
        }
    }

    public static OrderItemResponse from(OrderItemDto orderItemDto) {
        List<OrderItemOption> options = orderItemDto.getItemOptionDtos().stream().map(OrderItemOption::from).toList();
        return OrderItemResponse.builder()
                .productId(orderItemDto.getProductId())
                .productVariantId(orderItemDto.getProductVariantId())
                .productName(orderItemDto.getProductName())
                .thumbNailUrl(orderItemDto.getThumbnailUrl())
                .quantity(orderItemDto.getQuantity())
                .unitPrice(OrderItemPrice.from(orderItemDto.getUnitPrice()))
                .lineTotal(orderItemDto.getLineTotal())
                .options(options)
                .build();

    }
}

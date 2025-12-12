package com.example.order_service.api.cart.application.dto.result;

import com.example.order_service.api.cart.domain.service.dto.CartItemDto;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartItemResponse {
    private Long id;
    private Long productId;
    private Long productVariantId;
    private String productName;
    private String thumbnailUrl;
    private int quantity;
    private CartItemPrice price;
    private long lineTotal;
    private List<CartItemOption> options;
    private boolean isAvailable;

    @Getter
    @Builder
    public static class CartItemPrice {
        private long originalPrice;
        private int discountRate;
        private long discountAmount;
        private long discountedPrice;
    }

    @Getter
    @Builder
    public static class CartItemOption {
        private String optionTypeName;
        private String optionValueName;
    }

    @Builder
    private CartItemResponse(Long id, Long productVariantId, Long productId, String productName, String thumbnailUrl, int quantity,
                             CartItemPrice price, long lineTotal, List<CartItemOption> options, boolean isAvailable){
        this.id = id;
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.thumbnailUrl = thumbnailUrl;
        this.quantity = quantity;
        this.price = price;
        this.lineTotal = lineTotal;
        this.options = options;
        this.isAvailable = isAvailable;
    }

    public static CartItemResponse of(CartItemDto cartItemDto, CartProductResponse product){
        return CartItemResponse.builder()
                .id(cartItemDto.getId())
                .productId(product.getProductId())
                .productVariantId(cartItemDto.getProductVariantId())
                .productName(product.getProductName())
                .thumbnailUrl(product.getThumbnailUrl())
                .quantity(cartItemDto.getQuantity())
                .price(mapToPrice(product.getUnitPrice()))
                .lineTotal(product.getUnitPrice().getDiscountedPrice() * cartItemDto.getQuantity())
                .options(mapToOptions(product.getItemOptions()))
                .isAvailable(true)
                .build();
    }

    private static List<CartItemOption> mapToOptions(List<CartProductResponse.ItemOption> optionResponses){
        return optionResponses.stream().map(optionResponse -> CartItemOption.builder()
                .optionTypeName(optionResponse.getOptionTypeName())
                .optionValueName(optionResponse.getOptionValueName())
                .build()).toList();
    }

    private static CartItemPrice mapToPrice(CartProductResponse.UnitPrice unitPrice){
        return CartItemPrice.builder()
                .originalPrice(unitPrice.getOriginalPrice())
                .discountRate(unitPrice.getDiscountRate())
                .discountAmount(unitPrice.getDiscountAmount())
                .discountedPrice(unitPrice.getDiscountedPrice())
                .build();
    }

    public static CartItemResponse ofUnavailable(Long id, int quantity){
        return CartItemResponse
                .builder()
                .id(id)
                .productId(null)
                .productVariantId(null)
                .productName("정보를 불러올 수 없거나 판매 중지된 상품입니다")
                .thumbnailUrl(null)
                .quantity(quantity)
                .price(null)
                .lineTotal(0)
                .options(null)
                .isAvailable(false)
                .build();
    }
}

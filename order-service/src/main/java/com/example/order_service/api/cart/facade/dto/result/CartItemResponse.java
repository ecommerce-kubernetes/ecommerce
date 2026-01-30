package com.example.order_service.api.cart.facade.dto.result;

import com.example.order_service.api.cart.domain.service.dto.result.CartItemDto;
import com.example.order_service.api.cart.domain.service.dto.result.CartProductInfo;
import com.example.order_service.api.cart.domain.service.dto.result.CartProductInfo.ProductOption;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CartItemResponse {
    private Long id;
    private CartItemStatus status;
    private boolean isAvailable;
    private Long productId;
    private Long productVariantId;
    private String productName;
    private String thumbnailUrl;
    private int quantity;
    private CartItemPrice price;
    private long lineTotal;
    private List<CartItemOption> options;

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

    public static CartItemResponse available(CartItemDto cartItemDto, CartProductInfo product) {
        return CartItemResponse.builder()
                .id(cartItemDto.getId())
                .status(CartItemStatus.AVAILABLE)
                .isAvailable(true)
                .productId(product.getProductId())
                .productVariantId(cartItemDto.getProductVariantId())
                .productName(product.getProductName())
                .thumbnailUrl(product.getThumbnail())
                .quantity(cartItemDto.getQuantity())
                .price(mapToPrice(product))
                .lineTotal(product.getDiscountedPrice() * cartItemDto.getQuantity())
                .options(mapToOptions(product.getProductOption()))
                .build();

    }

//    public static CartItemResponse stop_sale(CartItemDto cartItemDto, CartProductResponse product) {
//        return CartItemResponse.builder()
//                .id(cartItemDto.getId())
//                .status(CartItemStatus.STOP_SALE)
//                .isAvailable(false)
//                .productId(product.getProductId())
//                .productVariantId(cartItemDto.getProductVariantId())
//                .productName(product.getProductName())
//                .thumbnailUrl(product.getThumbnailUrl())
//                .quantity(cartItemDto.getQuantity())
//                .price(mapToPrice(product.getUnitPrice()))
//                .lineTotal(product.getUnitPrice().getDiscountedPrice() * cartItemDto.getQuantity())
//                .options(mapToOptions(product.getProductOptionInfos()))
//                .build();
//    }

//    public static CartItemResponse preparing(CartItemDto cartItemDto, CartProductResponse product) {
//        return CartItemResponse.builder()
//                .id(cartItemDto.getId())
//                .status(CartItemStatus.PREPARING)
//                .isAvailable(false)
//                .productId(product.getProductId())
//                .productVariantId(cartItemDto.getProductVariantId())
//                .productName(product.getProductName())
//                .thumbnailUrl(product.getThumbnailUrl())
//                .quantity(cartItemDto.getQuantity())
//                .price(mapToPrice(product.getUnitPrice()))
//                .lineTotal(product.getUnitPrice().getDiscountedPrice() * cartItemDto.getQuantity())
//                .options(mapToOptions(product.getProductOptionInfos()))
//                .build();
//    }

//    public static CartItemResponse deleted(CartItemDto cartItemDto, CartProductResponse product) {
//        return CartItemResponse.builder()
//                .id(cartItemDto.getId())
//                .status(CartItemStatus.DELETED)
//                .isAvailable(false)
//                .productId(product.getProductId())
//                .productVariantId(cartItemDto.getProductVariantId())
//                .productName(product.getProductName())
//                .thumbnailUrl(product.getThumbnailUrl())
//                .quantity(cartItemDto.getQuantity())
//                .price(mapToPrice(product.getUnitPrice()))
//                .lineTotal(product.getUnitPrice().getDiscountedPrice() * cartItemDto.getQuantity())
//                .options(mapToOptions(product.getProductOptionInfos()))
//                .build();
//    }


    public static CartItemResponse unAvailable(Long id, Long productVariantId, int quantity){
        return CartItemResponse
                .builder()
                .id(id)
                .status(CartItemStatus.NOT_FOUND)
                .isAvailable(false)
                .productId(null)
                .productVariantId(productVariantId)
                .productName("정보를 불러올 수 없습니다")
                .thumbnailUrl(null)
                .quantity(quantity)
                .price(null)
                .lineTotal(0)
                .options(null)
                .build();
    }

//    private static List<CartItemOption> mapToOptions(List<CartProductResponse.ProductOptionInfo> optionResponses){
//        return optionResponses.stream().map(optionResponse -> CartItemOption.builder()
//                .optionTypeName(optionResponse.getOptionTypeName())
//                .optionValueName(optionResponse.getOptionValueName())
//                .build()).toList();
//    }

    private static CartItemPrice mapToPrice(CartProductResponse.UnitPrice unitPrice){
        return CartItemPrice.builder()
                .originalPrice(unitPrice.getOriginalPrice())
                .discountRate(unitPrice.getDiscountRate())
                .discountAmount(unitPrice.getDiscountAmount())
                .discountedPrice(unitPrice.getDiscountedPrice())
                .build();
    }

    private static CartItemPrice mapToPrice(CartProductInfo product) {
        return CartItemPrice.builder()
                .originalPrice(product.getOriginalPrice())
                .discountRate(product.getDiscountRate())
                .discountAmount(product.getDiscountAmount())
                .discountedPrice(product.getDiscountedPrice())
                .build();
    }

    private static List<CartItemOption> mapToOptions(List<ProductOption> optionResponses){
        return optionResponses.stream().map(optionResponse -> CartItemOption.builder()
                .optionTypeName(optionResponse.getOptionTypeName())
                .optionValueName(optionResponse.getOptionValueName())
                .build()).toList();
    }
}

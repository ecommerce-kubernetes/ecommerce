package com.example.order_service.api.cart.application.dto.result;

import com.example.order_service.api.cart.domain.service.dto.CartItemDto;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.dto.response.UnitPrice;
import com.example.order_service.service.client.dto.ProductResponse;
import lombok.*;

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
    private UnitPrice unitPrice;
    private long lineTotal;
    private List<ItemOptionResponse> options;
    private boolean isAvailable;

    @Builder
    private CartItemResponse(Long id, Long productVariantId, Long productId, String productName, String thumbnailUrl, int quantity,
                             UnitPrice unitPrice, long lineTotal, List<ItemOptionResponse> options, boolean isAvailable){
        this.id = id;
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.thumbnailUrl = thumbnailUrl;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        this.options = options;
        this.isAvailable = isAvailable;
    }

    public static CartItemResponse of(CartItemDto cartItemDto, ProductResponse product){
        return CartItemResponse.builder()
                .id(cartItemDto.getId())
                .productId(product.getProductId())
                .productVariantId(cartItemDto.getProductVariantId())
                .productName(product.getProductName())
                .thumbnailUrl(product.getThumbnailUrl())
                .quantity(cartItemDto.getQuantity())
                .unitPrice(product.getUnitPrice())
                .lineTotal(cartItemDto.getQuantity() * product.getUnitPrice().getDiscountedPrice())
                .options(product.getItemOptions())
                .isAvailable(true)
                .build();
    }

    public static CartItemResponse of(Long id, int quantity, ProductResponse response){
        return CartItemResponse
                .builder()
                .id(id)
                .productId(response.getProductId())
                .productName(response.getProductName())
                .thumbnailUrl(response.getThumbnailUrl())
                .quantity(quantity)
                .unitPrice(response.getUnitPrice())
                .lineTotal(response.getUnitPrice().getDiscountedPrice() * quantity)
                .options(response.getItemOptions())
                .isAvailable(true)
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
                .unitPrice(null)
                .lineTotal(0)
                .options(null)
                .isAvailable(false)
                .build();
    }
}

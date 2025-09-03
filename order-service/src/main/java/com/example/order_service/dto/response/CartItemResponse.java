package com.example.order_service.dto.response;

import com.example.order_service.entity.CartItems;
import com.example.order_service.service.client.dto.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long productId;
    private Long productVariantId;
    private String productName;
    private String thumbNailUrl;
    private List<ItemOptionResponse> options;
    private int price;
    private int discountRate;
    private int quantity;

    public CartItemResponse(CartItems cartItem, ProductResponse response){
        this.id = cartItem.getId();
        this.productId = response.getProductId();
        this.productVariantId = cartItem.getProductVariantId();
        this.productName = response.getProductName();
        this.thumbNailUrl = response.getThumbnailUrl();
        this.options = response.getItemOptions();
        this.price = response.getPrice();
        this.discountRate = response.getDiscountRate();
        this.quantity = cartItem.getQuantity();
    }
}

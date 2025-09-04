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
    private ProductInfo productInfo;
    private int quantity;
    private boolean isAvailable;

    public CartItemResponse(CartItems cartItem, ProductResponse response, boolean isAvailable){
        this.id = cartItem.getId();
        this.productInfo = createProductInfo(response);
        this.quantity = cartItem.getQuantity();
        this.isAvailable = isAvailable;
    }

    private ProductInfo createProductInfo(ProductResponse response){
        return new ProductInfo(response.getProductId(),
                response.getProductVariantId(),
                response.getProductName(),
                response.getPrice(),
                response.getDiscountRate(),
                response.getThumbnailUrl(),
                response.getItemOptions());
    }
}

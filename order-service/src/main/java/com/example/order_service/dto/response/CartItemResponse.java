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

    public CartItemResponse(CartItems cartItem, ProductResponse response){
        this.id = cartItem.getId();
        this.quantity = cartItem.getQuantity();
        this.isAvailable = (response != null);
        this.productInfo = isAvailable ? createProductInfo(response) : null;
    }

    private ProductInfo createProductInfo(ProductResponse response){
        if(response == null) return null;
        return new ProductInfo(response.getProductId(),
                response.getProductVariantId(),
                response.getProductName(),
                response.getProductPrice().getUnitPrice(),
                response.getProductPrice().getDiscountRate(),
                response.getThumbnailUrl(),
                response.getItemOptions());
    }

    public long getItemTotalPrice() {
        if(!isAvailable || productInfo == null){
            return 0;
        }
        return productInfo.calcDiscountPrice() * quantity;
    }
}

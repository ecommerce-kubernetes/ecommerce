package com.example.order_service.cart.application.facade;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.external.dto.CartProductListResult;
import com.example.order_service.cart.application.external.dto.CartProductResult;
import com.example.order_service.cart.application.external.dto.CartProductStatus;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.application.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CartItemValidator {

    public void validate(AddCartItemsCommand command, CartProductListResult productData) {
        Map<Long, CartProductResult> productDataMap = productData.toMap();
        for (AddCartItemsCommand.Item item : command.items()) {
            CartProductResult product = productDataMap.get(item.productVariantId());
            if (product == null) {
                throw new BusinessException(CartErrorCode.PRODUCT_NOT_FOUND);
            }
            if (product.status() != CartProductStatus.ON_SALE) {
                throw new BusinessException(CartErrorCode.PRODUCT_NOT_ON_SALE);
            }
        }
    }
}

package com.example.order_service.cart.application.facade;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.external.dto.CartProductResult;
import com.example.order_service.cart.application.external.dto.CartProductStatus;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.application.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CartItemValidator {

    public void validate(AddCartItemsCommand command, Map<Long, CartProductResult> productResultMap) {
        for (AddCartItemsCommand.Item item : command.items()) {
            CartProductResult product = productResultMap.get(item.productVariantId());
            if (product == null) {
                throw new BusinessException(CartErrorCode.CART_PRODUCT_NOT_FOUND);
            }
            if (product.status() != CartProductStatus.ON_SALE) {
                throw new BusinessException(CartErrorCode.CART_PRODUCT_CANNOT_ADD);
            }
            if (product.stock() < item.quantity()) {
                throw new BusinessException(CartErrorCode.CART_PRODUCT_STOCK_INSUFFICIENT);
            }
        }
    }
}

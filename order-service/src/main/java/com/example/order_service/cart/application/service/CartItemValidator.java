package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.cart.application.port.dto.CartProductStatus;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartItemValidator {

    public void validatePurchasable(CartProductResult.CartProductDetail product) {
        if (product == null) {
            throw new BusinessException(CartErrorCode.PRODUCT_NOT_FOUND);
        }
        if (product.status() != CartProductStatus.ON_SALE) {
            throw new BusinessException(CartErrorCode.PRODUCT_NOT_ON_SALE);
        }
    }
}

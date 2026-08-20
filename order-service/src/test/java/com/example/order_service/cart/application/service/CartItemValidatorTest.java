package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.fixture.CartProductFixture;
import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.cart.application.port.dto.CartProductStatus;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartItemValidatorTest {

    CartItemValidator validator = new CartItemValidator();
    
    @Test
    @DisplayName("누락된 상품이 존재하면 예외가 발생한다")
    void validateAddable_whenMissingProduct_thenThrownException() {
        //given
        CartProductResult.CartProductDetail product = null;
        //when
        //then
        assertThatThrownBy(() -> validator.validateAddable(product))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("장바구니에 추가할 수 없는 상품이 존재하면 예외가 발생한다")
    void validateAddable_whenCannotAddableProduct_thenThrownException() {
        //given
        CartProductResult.CartProductDetail product = CartProductFixture.anProduct().status(CartProductStatus.STOP_SALE).build();
        //when
        //then
        assertThatThrownBy(() -> validator.validateAddable(product))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.PRODUCT_NOT_ON_SALE);
    }
}
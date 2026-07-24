package com.example.order_service.cart.application.facade;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.external.dto.CartProductResult;
import com.example.order_service.cart.application.external.dto.CartProductStatus;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.BusinessException;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;

class CartItemValidatorTest {

    CartItemValidator validator = new CartItemValidator();
    
    @Test
    @DisplayName("누락된 상품이 존재하면 예외가 발생한다")
    void validate_missing_product() {
        //given
        AddCartItemsCommand.Item item1 = Instancio.of(AddCartItemsCommand.Item.class)
                .set(field("productVariantId"), 1L)
                .create();
        AddCartItemsCommand.Item item2 = Instancio.of(AddCartItemsCommand.Item.class)
                .set(field("productVariantId"), 2L)
                .create();
        AddCartItemsCommand command = Instancio.of(AddCartItemsCommand.class)
                .set(field("items"), List.of(item1, item2))
                .create();

        CartProductResult.CartProductDetail product1 = Instancio.of(CartProductResult.CartProductDetail.class)
                .set(field("productVariantId"), item1.productVariantId())
                .set(field("status"), CartProductStatus.ON_SALE)
                .set(field("stock"), item1.quantity() + 100)
                .create();
        CartProductResult productData = Instancio.of(CartProductResult.class)
                .set(field("products"), List.of(product1))
                .create();
        //when
        //then
        assertThatThrownBy(() -> validator.validate(command, productData))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("장바구니에 추가할 수 없는 상품이 존재하면 예외가 발생한다")
    void validate_cannot_add_product() {
        //given
        AddCartItemsCommand.Item item1 = Instancio.of(AddCartItemsCommand.Item.class)
                .set(field("productVariantId"), 1L)
                .create();
        AddCartItemsCommand.Item item2 = Instancio.of(AddCartItemsCommand.Item.class)
                .set(field("productVariantId"), 2L)
                .create();
        AddCartItemsCommand command = Instancio.of(AddCartItemsCommand.class)
                .set(field("items"), List.of(item1, item2))
                .create();

        CartProductResult.CartProductDetail product1 = Instancio.of(CartProductResult.CartProductDetail.class)
                .set(field("productVariantId"), item1.productVariantId())
                .set(field("status"), CartProductStatus.ON_SALE)
                .set(field("stock"), item1.quantity() + 100)
                .create();

        CartProductResult.CartProductDetail product2 = Instancio.of(CartProductResult.CartProductDetail.class)
                .set(field("productVariantId"), item2.productVariantId())
                .set(field("status"), CartProductStatus.STOP_SALE)
                .set(field("stock"), item2.quantity() + 100)
                .create();

        CartProductResult productData = Instancio.of(CartProductResult.class)
                .set(field("products"), List.of(product1, product2))
                .create();
        //when
        //then
        assertThatThrownBy(() -> validator.validate(command, productData))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.PRODUCT_NOT_ON_SALE);
    }
}
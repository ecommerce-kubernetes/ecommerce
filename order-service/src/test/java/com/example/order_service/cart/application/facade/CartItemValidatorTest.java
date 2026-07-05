package com.example.order_service.cart.application.facade;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.external.dto.CartProductResult;
import com.example.order_service.cart.application.external.dto.CartProductStatus;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.application.BusinessException;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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

        CartProductResult product1 = Instancio.of(CartProductResult.class)
                .set(field("productVariantId"), item1.productVariantId())
                .set(field("status"), CartProductStatus.ON_SALE)
                .set(field("stock"), item1.quantity() + 100)
                .create();
        Map<Long, CartProductResult> resultMap = Map.of(product1.productVariantId(), product1);
        //when
        //then
        assertThatThrownBy(() -> validator.validate(command, resultMap))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.CART_PRODUCT_NOT_FOUND);
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

        CartProductResult product1 = Instancio.of(CartProductResult.class)
                .set(field("productVariantId"), item1.productVariantId())
                .set(field("status"), CartProductStatus.ON_SALE)
                .set(field("stock"), item1.quantity() + 100)
                .create();

        CartProductResult product2 = Instancio.of(CartProductResult.class)
                .set(field("productVariantId"), item2.productVariantId())
                .set(field("status"), CartProductStatus.STOP_SALE)
                .set(field("stock"), item2.quantity() + 100)
                .create();

        Map<Long, CartProductResult> resultMap = Map.of(product1.productVariantId(), product1,
                product2.productVariantId(), product2);
        //when
        //then
        assertThatThrownBy(() -> validator.validate(command, resultMap))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.CART_PRODUCT_CANNOT_ADD);
    }

    @Test
    @DisplayName("재고 수량이 부족하면 예외가 발생한다")
    void validate_insufficient_stock() {
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

        CartProductResult product1 = Instancio.of(CartProductResult.class)
                .set(field("productVariantId"), item1.productVariantId())
                .set(field("status"), CartProductStatus.ON_SALE)
                .set(field("stock"), item1.quantity() + 100)
                .create();
        CartProductResult product2 = Instancio.of(CartProductResult.class)
                .set(field("productVariantId"), item2.productVariantId())
                .set(field("status"), CartProductStatus.ON_SALE)
                .set(field("stock"), item2.quantity() - 10)
                .create();
        Map<Long, CartProductResult> resultMap = Map.of(product1.productVariantId(), product1,
                product2.productVariantId(), product2);
        //when
        //then
        assertThatThrownBy(() -> validator.validate(command, resultMap))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.CART_PRODUCT_STOCK_INSUFFICIENT);
    }
}
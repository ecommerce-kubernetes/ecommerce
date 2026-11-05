package com.example.order_service.cart.domain;

import com.example.order_service.cart.domain.context.AddCartItemsContext;
import com.example.order_service.cart.domain.fixture.CartItemFixtureBuilder;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CartItemTest {

    private final IdGenerator idGenerator = new TsidGenerator();

    @Test
    @DisplayName("장바구니 항목을 생성한다")
    void create(){
        //given
        AddCartItemsContext.Item itemCtx = AddCartItemsContext.Item.builder()
                .productVariantId(1L)
                .quantity(3)
                .maxLimit(100)
                .build();
        //when
        CartItem item = CartItem.create(itemCtx, idGenerator);
        //then
        assertThat(item)
                .extracting(CartItem::getProductVariantId, CartItem::getQuantity)
                .containsExactly(1L, 3);
    }

    @Test
    @DisplayName("아이디 생성기가 누락되면 예외가 발생한다.")
    void create_whenIdGeneratorIsNull_thenThrownException() {
        //given
        AddCartItemsContext.Item itemCtx = AddCartItemsContext.Item.builder()
                .productVariantId(1L)
                .quantity(3)
                .maxLimit(100)
                .build();
        //when
        //then
        assertThatThrownBy(() -> CartItem.create(itemCtx, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("아이디 생성기는 필수 입니다.");
    }

    @Test
    @DisplayName("아이디 생성기가 null 아이디를 생성하면 예외가 발생한다.")
    void create_whenIdGeneratorGenerateNullId_thenThrownException() {
        //given
        AddCartItemsContext.Item itemCtx = AddCartItemsContext.Item.builder()
                .productVariantId(1L)
                .quantity(3)
                .maxLimit(100)
                .build();
        IdGenerator nullIdGenerator = () -> null;
        //when
        //then
        assertThatThrownBy(() -> CartItem.create(itemCtx, nullIdGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니 항목 생성시 식별자는 필수입니다.");
    }

    @Test
    @DisplayName("장바구니 항목을 생성할때 수량이 1보다 작으면 예외가 발생한다.")
    void create_whenQuantityLessThanOne_thenThrownException(){
        //given
        AddCartItemsContext.Item itemCtx = AddCartItemsContext.Item.builder()
                .productVariantId(1L)
                .quantity(0)
                .maxLimit(100)
                .build();
        //when
        //then
        assertThatThrownBy(() -> CartItem.create(itemCtx, idGenerator))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.INVALID_CART_ITEM_QUANTITY);
    }

    @Test
    @DisplayName("장바구니 항목을 생성할 때 수량이 최대 한계치를 초과하면 예외가 발생한다.")
    void create_whenQuantityExceedMaxLimit_thenThrownException(){
        //given
        AddCartItemsContext.Item itemCtx = AddCartItemsContext.Item.builder()
                .productVariantId(1L)
                .quantity(15)
                .maxLimit(10)
                .build();
        //when
        //then
        assertThatThrownBy(() -> CartItem.create(itemCtx, idGenerator))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.QUANTITY_EXCEED_MAX_LIMIT);
    }

    @Test
    @DisplayName("수량을 추가한다")
    void addQuantity(){
        //given
        CartItem cartItem = CartItemFixtureBuilder.given().withQuantity(3).build();
        //when
        cartItem.addQuantity(2, 10);
        //then
        assertThat(cartItem.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("수량을 추가할때 추가할 수량이 1보다 작으면 예외가 발생한다.")
    void addQuantity_whenQuantityLessThanOne_thenThrownException() {
        //given
        CartItem cartItem = CartItemFixtureBuilder.given().withQuantity(3).build();
        //when
        //then
        assertThatThrownBy(() -> cartItem.addQuantity(-1, 10))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.INVALID_CART_ITEM_QUANTITY);
    }

    @Test
    @DisplayName("수량이 최대 한계치를 초과하면 예외가 발생한다")
    void addQuantity_whenQuantityExceedMaxLimit_thenThrownException(){
        //given
        CartItem cartItem = CartItemFixtureBuilder.given().withQuantity(3).build();
        //when
        //then
        assertThatThrownBy(() -> cartItem.addQuantity(10, 10))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.QUANTITY_EXCEED_MAX_LIMIT);
    }

    @Test
    @DisplayName("장바구니 항목의 수량을 변경한다")
    void updateQuantity(){
        //given
        CartItem cartItem = CartItemFixtureBuilder.given().withQuantity(3).build();
        //when
        cartItem.updateQuantity(5, 100);
        //then
        assertThat(cartItem.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("항목 수량을 수정할때 상품 수량이 1보다 작으면 예외가 발생한다.")
    void updateQuantity_whenQuantityLessThanOne_thenThrownException(){
        //given
        CartItem cartItem = CartItemFixtureBuilder.given().withQuantity(3).build();
        //when
        //then
        assertThatThrownBy(() -> cartItem.updateQuantity(0, 100))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.INVALID_CART_ITEM_QUANTITY);
    }

    @Test
    @DisplayName("수량을 변경할때 수량이 최대 한계치를 초과하는 경우 예외가 발생한다.")
    void updateQuantity_whenQuantityExceedMaxLimit_thenThrownException(){
        //given
        CartItem cartItem = CartItemFixtureBuilder.given().withQuantity(3).build();
        //when
        //then
        assertThatThrownBy(() -> cartItem.updateQuantity(50, 10))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.QUANTITY_EXCEED_MAX_LIMIT);
    }
}

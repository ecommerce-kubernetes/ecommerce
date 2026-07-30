package com.example.order_service.cart.domain;

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
        //when
        CartItem item = CartItem.create(1L, 3, 100, idGenerator);
        //then
        assertThat(item)
                .extracting(CartItem::getProductVariantId, CartItem::getQuantity)
                .containsExactly(1L, 3);
    }

    @Test
    @DisplayName("상품 판매 단위 식별자가 없으면 예외가 발생한다")
    void create_productVariantId_null(){
        //given
        //when
        //then
        assertThatThrownBy(() -> CartItem.create(null, 3, 100, idGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니 항목 생성시 상품 변형 아이디는 필수입니다.");
    }

    @Test
    @DisplayName("아이디 생성기가 없으면 예외가 발생한다.")
    void create_idGenerator_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> CartItem.create(1L, 3, 100, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("아이디 생성기는 필수 입니다.");
    }

    @Test
    @DisplayName("아이디가 누락되면 예외가 발생한다.")
    void create_id_null() {
        //given
        IdGenerator nullIdGenerator = () -> null;
        //when
        //then
        assertThatThrownBy(() -> CartItem.create(1L, 3, 100, nullIdGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니 항목 생성시 식별자는 필수입니다.");
    }

    @Test
    @DisplayName("수량이 1미만인 항목은 생성할 수 없다")
    void create_quantity_less_than_1(){
        //given
        //when
        //then
        assertThatThrownBy(() -> CartItem.create(1L, 0, 100, idGenerator))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.INVALID_CART_ITEM_QUANTITY);
    }

    @Test
    @DisplayName("수량이 최대 한계치를 초과하면 예외가 발생한다")
    void create_quantity_exceed_maxLimit(){
        //given
        //when
        //then
        assertThatThrownBy(() -> CartItem.create(1L, 100, 50, idGenerator))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.QUANTITY_EXCEED_MAX_LIMIT);
    }

    @Test
    @DisplayName("수량을 추가한다")
    void addQuantity(){
        //given
        CartItem cartItem = CartItem.create(1L, 3, 100, idGenerator);
        //when
        cartItem.addQuantity(2, 10);
        //then
        assertThat(cartItem.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("수량이 최대 한계치를 초과하면 예외가 발생한다")
    void addQuantity_quantity_exceed_maxLimit(){
        //given
        CartItem cartItem = CartItem.create(1L, 3, 100, idGenerator);
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
        CartItem cartItem = CartItem.create(1L, 3, 100, idGenerator);
        //when
        cartItem.updateQuantity(5, 100);
        //then
        assertThat(cartItem.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("항목 수량을 수정할때 상품 수량을 1 미만으로 변경할 수 없다")
    void updateQuantityWhenQuantityLessThan1(){
        //given
        CartItem cartItem = CartItem.create(1L, 3, 100, idGenerator);
        //when
        //then
        assertThatThrownBy(() -> cartItem.updateQuantity(0, 100))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.INVALID_CART_ITEM_QUANTITY);
    }

    @Test
    @DisplayName("항목 수량을 수정할때 최대 한계치를 초과할 수 없다.")
    void updateQuantity_quantity_exceed_maxLimit(){
        //given
        CartItem cartItem = CartItem.create(1L, 3, 100, idGenerator);
        //when
        //then
        assertThatThrownBy(() -> cartItem.updateQuantity(50, 10))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.QUANTITY_EXCEED_MAX_LIMIT);
    }
}

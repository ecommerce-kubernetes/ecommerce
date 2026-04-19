package com.example.order_service.api.cart.facade.dto.result;

import com.example.order_service.api.support.BaseTestSupport;
import com.example.order_service.api.support.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CartResultTest extends BaseTestSupport {

    @Test
    @DisplayName("장바구니 총 가격 정보를 계산한다")
    void calculateCartPrice(){
        //given
        CartResult.CartItemResult item1 = TestUtil.sample(fixtureMonkey.giveMeBuilder(CartResult.CartItemResult.class)
                .set("isAvailable", true)
                .set("quantity", 2)
                .set("price.originalPrice", 10000)
                .set("price.discountAmount", 1000)
                .set("price.discountedPrice", 9000)
                .set("lineTotal", 18000));
        CartResult.CartItemResult item2 = TestUtil.sample(fixtureMonkey.giveMeBuilder(CartResult.CartItemResult.class)
                .set("isAvailable", true)
                .set("quantity", 1)
                .set("price.originalPrice", 5000)
                .set("price.discountAmount", 500)
                .set("price.discountedPrice", 4500)
                .set("lineTotal", 4500));
        //when
        CartResult.Cart result = CartResult.Cart.from(List.of(item1, item2));
        //then
        assertThat(result.totalOriginalPrice()).isEqualTo(25000);
        assertThat(result.totalDiscountAmount()).isEqualTo(2500);
        assertThat(result.totalFinalPrice()).isEqualTo(22500);
    }
}

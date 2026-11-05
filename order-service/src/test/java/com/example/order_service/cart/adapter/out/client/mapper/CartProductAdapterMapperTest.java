package com.example.order_service.cart.adapter.out.client.mapper;

import com.example.order_service.cart.application.fixture.CartProductFixture;
import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
import com.example.order_service.infrastructure.fixture.ProductResponseFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CartProductAdapterMapperTest {

    private final CartProductAdapterMapper cartProductAdapterMapper = new CartProductAdapterMapper();

    @Test
    @DisplayName("장바구니 상품 조회 결과 매핑")
    void toCartProductResult() {
        //given
        ProductResponse response = ProductResponseFixture.anProductResponse().build();
        CartProductResult expectedResult = CartProductFixture.anProducts().build();
        //when
        CartProductResult result = cartProductAdapterMapper.toCartProductResult(response);
        //then
        assertThat(result.products()).hasSize(1);
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedResult);
    }
}

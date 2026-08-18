package com.example.order_service.cart.adaptor.out.client.mapper;

import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.cart.application.port.dto.CartProductStatus;
import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.instancio.Select.field;

public class CartProductAdapterMapperTest {

    private final CartProductAdapterMapper cartProductAdapterMapper = new CartProductAdapterMapper();

    @Test
    @DisplayName("장바구니 상품 조회 결과 매핑")
    void toCartProductResult() {
        //given
        ProductResponse.UnitPrice unitPrice = ProductResponse.UnitPrice.builder()
                .originalPrice(10000L)
                .discountRate(10)
                .discountAmount(1000L)
                .discountedPrice(9000L)
                .build();

        ProductResponse.ProductDetail product = Instancio.of(ProductResponse.ProductDetail.class)
                .set(field("status"), "ON_SALE")
                .set(field("unitPrice"), unitPrice)
                .create();

        ProductResponse response = ProductResponse.builder()
                .products(List.of(product))
                .build();
        //when
        CartProductResult result = cartProductAdapterMapper.toCartProductResult(response);
        //then
        assertThat(result.products()).hasSize(1);
        assertThat(result.products())
                .extracting("productId", "status", "originalPrice")
                .containsExactly(tuple( product.productId(), CartProductStatus.ON_SALE, Money.wons(unitPrice.originalPrice())));
    }
}

package com.example.order_service.order.infrastructure.adaptor.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
import com.example.order_service.order.application.port.dto.OrderProductStatus;
import com.example.order_service.order.application.port.dto.OrderProductsResult;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.instancio.Select.field;

class OrderProductPortMapperTest {

    private final OrderProductPortMapper productPortMapper = new OrderProductPortMapper();

    @Test
    @DisplayName("주문 상품 조회 결과를 매핑한다")
    void mapToOrderProductsResult() {
        //given
        ProductResponse.UnitPrice unitPrice = ProductResponse.UnitPrice.builder()
                .originalPrice(10000L)
                .discountRate(10)
                .discountAmount(1000L)
                .discountedPrice(9000L)
                .build();

        ProductResponse.ProductOption option = ProductResponse.ProductOption.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL")
                .build();

        ProductResponse.ProductDetail product = Instancio.of(ProductResponse.ProductDetail.class)
                .set(field("unitPrice"), unitPrice)
                .set(field("status"), "ON_SALE")
                .set(field("options"), List.of(option))
                .create();

        ProductResponse response = ProductResponse.builder()
                .products(List.of(product))
                .build();
        //when
        OrderProductsResult result = productPortMapper.mapToOrderProductsResult(response);
        //then
        assertThat(result.products()).hasSize(1);
        assertThat(result.products())
                .extracting("productSnapshot.productId", "productSnapshot.productName")
                .containsExactly(tuple(product.productId(), product.productName()));

        assertThat(result.products())
                .extracting("status")
                .containsExactly(OrderProductStatus.ON_SALE);

        assertThat(result.products())
                .extracting("priceSnapshot.originalPrice", "priceSnapshot.discountRate")
                .containsExactly(tuple(Money.wons(10000L), 10));

        assertThat(result.products().getFirst().options())
                .extracting("optionTypeName", "optionValueName")
                .containsExactly(tuple(option.optionTypeName(), option.optionValueName()));
    }

    @Test
    @DisplayName("주문 상품 조회 결과가 없으면 빈 리스트로 매핑한다")
    void mapToOrderProductsResult_emptyProducts(){
        //given
        ProductResponse response = ProductResponse.builder()
                .products(null)
                .build();
        //when
        OrderProductsResult result = productPortMapper.mapToOrderProductsResult(response);
        //then
        assertThat(result.products()).isNotNull();
        assertThat(result.products()).isEmpty();
    }

    @Test
    @DisplayName("주문 상폼 조회 결과중 옵션이 없으면 옵션은 빈 리스트로 매핑한다")
    void mapToOrderProductsResult_empty_option(){
        //given
        ProductResponse.UnitPrice unitPrice = ProductResponse.UnitPrice.builder()
                .originalPrice(10000L)
                .discountRate(10)
                .discountAmount(1000L)
                .discountedPrice(9000L)
                .build();

        ProductResponse.ProductDetail product = Instancio.of(ProductResponse.ProductDetail.class)
                .set(field("unitPrice"), unitPrice)
                .set(field("status"), "ON_SALE")
                .set(field("options"), Collections.emptyList())
                .create();

        ProductResponse response = ProductResponse.builder()
                .products(List.of(product))
                .build();
        //when
        OrderProductsResult result = productPortMapper.mapToOrderProductsResult(response);
        //then
        assertThat(result.products().getFirst().options()).isNotNull();
        assertThat(result.products().getFirst().options()).isEmpty();
    }
}
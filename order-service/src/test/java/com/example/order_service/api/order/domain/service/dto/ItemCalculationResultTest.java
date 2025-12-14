package com.example.order_service.api.order.domain.service.dto;

import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemCalculationResultTest {

    @Test
    @DisplayName("주문 상품 수량과 상품 정보를 통해 상품 가격 정보를 생성한다")
    void of(){
        //given
        Map<Long, Integer> quantityByVariantId = Map.of(1L, 3, 2L, 5);
        Map<Long, OrderProductResponse.UnitPrice> unitPriceByVariantId = Map.of(
                1L, OrderProductResponse.UnitPrice.builder()
                        .originalPrice(3000L)
                        .discountRate(10)
                        .discountAmount(300L)
                        .discountedPrice(2700L).build(),
                2L, OrderProductResponse.UnitPrice.builder()
                        .originalPrice(5000L)
                        .discountRate(10)
                        .discountAmount(500L)
                        .discountedPrice(4500L).build()
        );
        //when
        ItemCalculationResult itemCalculationResult = ItemCalculationResult.of(quantityByVariantId, unitPriceByVariantId);
        //then
        assertThat(itemCalculationResult)
                .extracting("totalOriginalPrice", "totalProductDiscount", "subTotalPrice")
                .containsExactly(34000L, 3400L, 30600L);
    }
}

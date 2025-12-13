package com.example.order_service.api.order.domain;

import com.example.order_service.api.order.domain.service.dto.command.CouponSpec;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemSpec;
import com.example.order_service.api.support.ExcludeInfraServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class OrderDomainServiceTest extends ExcludeInfraServiceTest {

    @Test
    @DisplayName("주문을 저장한다")
    void saveOrder(){
        //given
        //when
        //then
    }

    private CouponSpec createCouponSpec(Long couponId, String couponName, Long discountAmount){
        return CouponSpec.builder()
                .couponId(couponId)
                .couponName(couponName)
                .discountAmount(discountAmount)
                .build();
    }

    private OrderItemSpec createOrderItemSpec(Long productId, Long productVariantId, String productName, String thumbnail,
                                              long originalPrice, int discountRate, int quantity, Map<String, String> optionMap){

        long discountAmount = originalPrice * discountRate / 100;
        return OrderItemSpec.builder()
                .productId(productId)
                .productVariantId(productVariantId)
                .productName(productName)
                .thumbnailUrl(thumbnail)
                .unitPrice(
                        OrderItemSpec.UnitPrice.builder()
                                .originalPrice(originalPrice)
                                .discountRate(discountRate)
                                .discountAmount(discountAmount)
                                .discountedPrice(originalPrice - discountAmount)
                                .build())
                .lineTotal((originalPrice - discountAmount) * quantity)
                .itemOptions(
                        optionMap.entrySet().stream().map(entry -> OrderItemSpec.ItemOption.builder()
                                .optionTypeName(entry.getKey())
                                .optionValueName(entry.getValue()).build()).toList())
                .build();
    }
}

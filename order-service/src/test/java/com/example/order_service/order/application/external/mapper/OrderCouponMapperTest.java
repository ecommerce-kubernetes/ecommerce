package com.example.order_service.order.application.external.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.command.CouponCommand;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.order.application.external.dto.command.OrderCouponCommand;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.application.external.mapper.OrderCouponMapper;
import com.example.order_service.order.application.external.mapper.OrderCouponMapperImpl;
import com.example.order_service.order.domain.vo.OrderCouponSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderCouponMapperTest {

    private final MoneyMapper moneyMapper = Mappers.getMapper(MoneyMapper.class);
    private final OrderCouponMapper mapper = new OrderCouponMapperImpl(moneyMapper);

    @Test
    @DisplayName("주문 커맨드를 쿠폰 서비스 커맨드로 매핑한다")
    void toCommand() {
        //given
        OrderCouponCommand.AppliedCouponItem item =
                OrderCouponCommand.AppliedCouponItem.of(1L, Money.wons(10000L), 2, 2L);
        OrderCouponCommand.Calculate command =
                OrderCouponCommand.Calculate.of(1L, 1L, List.of(item));

        CouponCommand.Item expectedItem = CouponCommand.Item.builder()
                .productVariantId(1L)
                .price(10000L)
                .quantity(2)
                .itemCouponId(2L)
                .build();

        CouponCommand.Calculate expected = CouponCommand.Calculate.builder()
                .userId(1L)
                .cartCouponId(1L)
                .items(List.of(expectedItem))
                .build();
        //when
        CouponCommand.Calculate result = mapper.toCommand(command);
        //then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("쿠폰 응답을 result로 매핑한다")
    void toResult() {
        //given
        CouponClientResponse.ItemCoupon itemCoupon = CouponClientResponse.ItemCoupon.builder()
                .productVariantId(1L)
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L)
                .build();
        CouponClientResponse.CartCoupon cartCoupon = CouponClientResponse.CartCoupon.builder()
                .couponId(2L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L)
                .build();
        CouponClientResponse.Calculate response = CouponClientResponse.Calculate.builder()
                .cartCoupon(cartCoupon)
                .itemCoupons(List.of(itemCoupon))
                .build();

        OrderCouponSnapshot expectedCartCoupon = OrderCouponSnapshot.of(2L, "1000원 할인 쿠폰", Money.wons(1000L));
        OrderCouponSnapshot resultItemCoupon = OrderCouponSnapshot.of(1L, "1000원 할인 쿠폰", Money.wons(1000L));
        OrderCouponResult.ItemCoupon expectedItemCoupon = OrderCouponResult.ItemCoupon.builder()
                .productVariantId(1L)
                .itemCoupon(resultItemCoupon)
                .build();
        OrderCouponResult.Calculate expectedResult = OrderCouponResult.Calculate.builder()
                .cartCoupon(expectedCartCoupon)
                .itemCoupons(List.of(expectedItemCoupon))
                .build();
        //when
        OrderCouponResult.Calculate result = mapper.toResult(response);
        //then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }
}

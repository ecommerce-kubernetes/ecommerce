package com.example.order_service.ordersheet.application.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.command.CouponCommand;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetCouponResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderSheetCouponMapperTest {

    private final MoneyMapper moneyMapper = Mappers.getMapper(MoneyMapper.class);
    private final OrderSheetCouponMapper mapper = new OrderSheetCouponMapperImpl(moneyMapper);

    @Test
    @DisplayName("주문 커맨드를 쿠폰 서비스 커맨드로 매핑한다")
    void toCommand() {
        //given
        OrderSheetCommand.AppliedCouponItem item =
                OrderSheetCommand.AppliedCouponItem.of(1L, Money.wons(10000L), 2, 2L);
        OrderSheetCommand.CouponCalculate command =
                OrderSheetCommand.CouponCalculate.of(1L, 1L, List.of(item));

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

        OrderSheetCouponResult.CartCoupon expectedCartCoupon = OrderSheetCouponResult.CartCoupon.builder()
                .couponId(2L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(Money.wons(1000L))
                .build();

        OrderSheetCouponResult.ItemCoupon expectedItemCoupon = OrderSheetCouponResult.ItemCoupon.builder()
                .productVariantId(1L)
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(Money.wons(1000L))
                .build();

        OrderSheetCouponResult.Calculate expectedResult = OrderSheetCouponResult.Calculate.builder()
                .cartCoupon(expectedCartCoupon)
                .itemCoupons(List.of(expectedItemCoupon))
                .build();
        //when
        OrderSheetCouponResult.Calculate result = mapper.toResult(response);
        //then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }
}

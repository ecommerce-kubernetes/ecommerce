package com.example.order_service.ordersheet.application.dto.command;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.ordersheet.exception.OrderSheetErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderSheetCommandTest {

    @Test
    @DisplayName("주문 상품이 없으면 예외가 발생한다")
    void items_not_exist(){
        //given
        //when
        //then
        assertThatThrownBy(() -> OrderSheetCommand.Create.builder()
                .userId(1L)
                .items(List.of())
                .cartCouponId(1L)
                .itemCoupons(List.of())
                .build())
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_ITEMS_REQUIRED);
    }

    @Test
    @DisplayName("중복된 상품이 있는경우 예외가 발생한다")
    void items_duplicated(){
        //given
        OrderSheetCommand.OrderItem item = OrderSheetCommand.OrderItem.builder()
                .productVariantId(1L)
                .quantity(2)
                .build();
        //when
        //then
        assertThatThrownBy(() -> OrderSheetCommand.Create.builder()
                .userId(1L)
                .cartCouponId(1L)
                .items(List.of(item, item))
                .itemCoupons(List.of())
                .build())
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_ITEMS_DUPLICATE);
    }

    @Test
    @DisplayName("쿠폰 적용 대상 상품이 주문 상품에 존재하지 않으면 예외가 발생한다")
    void coupon_item_not_in_items(){
        //given
        OrderSheetCommand.OrderItem item = OrderSheetCommand.OrderItem.builder()
                .productVariantId(1L)
                .quantity(5)
                .build();

        OrderSheetCommand.ItemCoupon itemCoupon = OrderSheetCommand.ItemCoupon.builder()
                .productVariantId(2L)
                .couponId(1L)
                .build();
        //when
        //then
        assertThatThrownBy(() -> OrderSheetCommand.Create.builder()
                .userId(1L)
                .cartCouponId(null)
                .items(List.of(item))
                .itemCoupons(List.of(itemCoupon))
                .build())
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_COUPON_ITEM_NOT_IN_ITEMS);
    }

    @Test
    @DisplayName("하나의 상품에 여러 쿠폰을 적용하면 예외가 발생한다")
    void duplicate_coupon_application(){
        //given
        OrderSheetCommand.OrderItem item = OrderSheetCommand.OrderItem.builder()
                .productVariantId(1L)
                .quantity(5)
                .build();
        OrderSheetCommand.ItemCoupon itemCoupon1 = OrderSheetCommand.ItemCoupon.builder()
                .productVariantId(1L)
                .couponId(1L)
                .build();
        OrderSheetCommand.ItemCoupon itemCoupon2 = OrderSheetCommand.ItemCoupon.builder()
                .productVariantId(1L)
                .couponId(1L)
                .build();
        //when
        //then
        assertThatThrownBy(() -> OrderSheetCommand.Create.builder()
                .userId(1L)
                .cartCouponId(null)
                .items(List.of(item))
                .itemCoupons(List.of(itemCoupon1, itemCoupon2))
                .build())
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_DUPLICATE_COUPON_APPLICATION);
    }

    @Test
    @DisplayName("하나의 쿠폰을 여러 상품에 적용하려는 경우 예외가 발생한다")
    void coupon_applied_multiple(){
        //given
        OrderSheetCommand.OrderItem item1 = OrderSheetCommand.OrderItem.builder()
                .productVariantId(1L)
                .quantity(5)
                .build();
        OrderSheetCommand.OrderItem item2 = OrderSheetCommand.OrderItem.builder()
                .productVariantId(2L)
                .quantity(5)
                .build();
        OrderSheetCommand.ItemCoupon itemCoupon1 = OrderSheetCommand.ItemCoupon.builder()
                .productVariantId(1L)
                .couponId(1L)
                .build();
        OrderSheetCommand.ItemCoupon itemCoupon2 = OrderSheetCommand.ItemCoupon.builder()
                .productVariantId(2L)
                .couponId(1L)
                .build();
        //when
        //then
        assertThatThrownBy(() -> OrderSheetCommand.Create.builder()
                .userId(1L)
                .cartCouponId(null)
                .items(List.of(item1, item2))
                .itemCoupons(List.of(itemCoupon1, itemCoupon2))
                .build())
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_ALREADY_APPLIED_TO_ANOTHER_ITEM);
    }
}
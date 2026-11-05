package com.example.order_service.order.application.service.fixture;

import com.example.order_service.order.application.service.ordersheet.dto.command.*;

import java.util.List;

public class OrderSheetCommandFixture {

    public static CreateCartOrderSheetCommand.CreateCartOrderSheetCommandBuilder anCreateCartCommand() {
        return CreateCartOrderSheetCommand.builder()
                .userId(1L)
                .cartItemIds(List.of(1L));
    }

    public static CreateDirectOrderSheetCommand.CreateDirectOrderSheetCommandBuilder anCreateDirectCommand() {
        CreateDirectOrderSheetCommand.OrderVariant item = CreateDirectOrderSheetCommand.OrderVariant.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();
        return CreateDirectOrderSheetCommand.builder()
                .userId(1L)
                .items(List.of(item));
    }

    public static UpdateOrderSheetShippingAddressCommand.UpdateOrderSheetShippingAddressCommandBuilder anUpdateShippingAddressCommand() {
        return UpdateOrderSheetShippingAddressCommand.builder()
                .orderSheetId(1L)
                .userId(1L)
                .receiverName("수령인")
                .receiverPhone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 테헤란로 123")
                .addressDetail("123동 1234호");
    }

    public static ApplyItemCouponsCommand.ApplyItemCouponsCommandBuilder anApplyItemCouponsCommand() {
        ApplyItemCouponsCommand.ItemCouponCommand itemCouponCommand = anItemCouponCommand().build();
        return ApplyItemCouponsCommand.builder()
                .userId(1L)
                .orderSheetId(1L)
                .itemCouponCommands(List.of(itemCouponCommand));
    }

    public static ApplyItemCouponsCommand.ItemCouponCommand.ItemCouponCommandBuilder anItemCouponCommand() {
        return ApplyItemCouponsCommand.ItemCouponCommand.builder()
                .orderSheetItemId(1L)
                .itemCouponId(1L);
    }

    public static ApplyCartCouponCommand.ApplyCartCouponCommandBuilder anApplyCartCouponCommand() {
        return ApplyCartCouponCommand.builder()
                .userId(1L)
                .orderSheetId(1L)
                .cartCouponId(1L);
    }

    public static ApplyPointCommand.ApplyPointCommandBuilder anApplyPointCommand() {
        return ApplyPointCommand.builder()
                .userId(1L)
                .orderSheetId(1L)
                .usedPoints(1000L);
    }
}

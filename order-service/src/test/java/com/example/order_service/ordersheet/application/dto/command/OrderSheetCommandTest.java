package com.example.order_service.ordersheet.application.dto.command;

import com.example.order_service.api.common.exception.business.BusinessException;
import com.example.order_service.api.common.exception.business.code.OrderSheetErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderSheetCommandTest {

    @Test
    @DisplayName("중복된 상품 Id로 Command 객체를 생성하면 예외가 발생한다")
    void create_duplicateVariantId() {
        //given
        OrderSheetCommand.OrderItem orderSheetItem = OrderSheetCommand.OrderItem.builder()
                .productVariantId(1L)
                .quantity(2)
                .build();
        //when
        //then
        assertThatThrownBy(() -> OrderSheetCommand.Create.builder()
                .userId(1L)
                .items(List.of(orderSheetItem, orderSheetItem)).build())
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_DUPLICATE_ITEMS);
    }

    @Test
    @DisplayName("상품이 없는 command 객체를 생성하면 예외가 발생한다")
    void create_emptyItems(){
        //given
        //when
        //then
        assertThatThrownBy(() -> OrderSheetCommand.Create.builder()
                .userId(1L)
                .items(List.of()).build())
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_ITEM_REQUIRED);
    }

}
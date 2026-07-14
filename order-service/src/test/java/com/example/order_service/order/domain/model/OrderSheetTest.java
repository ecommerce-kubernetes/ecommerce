package com.example.order_service.order.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderSheetTest {

    @Test
    @DisplayName("주문서를 생성하면 주문 항목을 토대로 가격이 계산된다.")
    void create() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        //when
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);
        //then
        assertThat(orderSheet.getOrderer()).isEqualTo(orderer);
        assertThat(orderSheet.getItems()).hasSize(1)
                        .containsExactly(item);

        assertThat(orderSheet.getExpiresAt()).isEqualTo(expiresAt);

        assertThat(orderSheet)
                .extracting(OrderSheet::getTotalOriginalPrice, OrderSheet::getTotalProductDiscountAmount,
                        OrderSheet::getTotalCouponDiscountAmount, OrderSheet::getUsedPoints, OrderSheet::getTotalPaymentAmount)
                .containsExactly(
                        item.getOriginalLineTotal(), item.getProductDiscountLineTotal(),
                        Money.ZERO, Money.ZERO, item.getFinalAmount()
                );
    }

    @Test
    @DisplayName("주문서를 생성할때 주문자 정보가 없으면 예외가 발생한다.")
    void create_orderer_null() {
        //given
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        //when
        //then
        assertThatThrownBy(() -> OrderSheet.create(null, List.of(item), expiresAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문서(OrderSheet) 생성시 주문자는 필수이다.");
    }

    @Test
    @DisplayName("주문서를 생성할때 주문 항목이 0개 이하인 경우 예외가 발생한다.")
    void create_items_empty() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        List<OrderSheetItem> items = Collections.emptyList();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        //when
        //then
        assertThatThrownBy(() -> OrderSheet.create(orderer, items, expiresAt))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_ITEMS_REQUIRED);
    }

    @Test
    @DisplayName("주문서를 생성할때 주문서 만료 시간이 없으면 예외가 발생한다.")
    void create_expiresAt_null() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        //when
        //then
        assertThatThrownBy(() -> OrderSheet.create(orderer, List.of(item), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문서(OrderSheet) 생성시 만료 시간은 필수이다.");
    }

    @Test
    @DisplayName("주문서의 배송 정보를 변경한다")
    void changeShippingAddress() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);

        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345",
                "서울시 테헤란로 123", "123동 1234호");
        //when
        orderSheet.changeShippingAddress(shippingAddress);
        //then
        assertThat(orderSheet.getShippingAddress()).isEqualTo(shippingAddress);
    }

    private OrderSheetItem createOrderSheetItem() {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        int quantity = 3;
        return OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList());
    }
}

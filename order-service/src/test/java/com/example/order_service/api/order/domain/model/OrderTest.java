package com.example.order_service.api.order.domain.model;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.domain.model.vo.OrderItemPrice;
import com.example.order_service.api.order.domain.model.vo.OrderPriceDetail;
import com.example.order_service.api.order.domain.model.vo.OrderedProduct;
import com.example.order_service.api.order.domain.model.vo.Orderer;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.order_service.api.support.fixture.OrderFixture.*;
import static com.example.order_service.api.support.fixture.OrderFixture.anOrderItemCreationContext;
import static com.example.order_service.api.support.fixture.OrderFixture.anOrderPriceSpec;
import static com.example.order_service.api.support.fixture.OrderFixture.anProductSpec;
import static org.assertj.core.api.Assertions.*;

public class OrderTest {

    @Nested
    @DisplayName("주문 생성")
    class Create {
        @Test
        @DisplayName("주문 생성시 쿠폰이 있는 경우 쿠폰 정보가 매핑된 주문을 생성한다")
        void createOrder(){
            //given
            OrderCreationContext context = anOrderCreationContext().build();
            //when
            Order order = Order.create(context);
            //then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getOrderName()).isEqualTo("상품");
            assertThat(order.getOrderNo()).startsWith("ORD-");

            // 주문자 정보
            assertThat(order.getOrderer())
                    .isEqualTo(Orderer.of(1L, "유저", "010-1234-5678"));

            // 주문 가격
            assertThat(order.getOrderPriceDetail())
                    .isEqualTo(OrderPriceDetail.of(10000L, 1000L, 1000L, 1000L, 7000L));

            // 배송지 정보
            assertThat(order.getDeliveryAddress()).isEqualTo("서울시 테헤란로 123");

            // 쿠폰 정보
            assertThat(order.getCoupon())
                    .extracting(Coupon::getCouponId, Coupon::getCouponName, Coupon::getDiscountAmount)
                    .containsExactly(1L, "1000원 할인 쿠폰", 1000L);

            // 주문 생성시 결제 정보는 없음
            assertThat(order.getValidPayment()).isNull();

            assertThat(order.getOrderItems()).hasSize(1)
                    .extracting(OrderItem::getOrderedProduct, OrderItem::getOrderItemPrice,
                            OrderItem::getLineTotal, OrderItem::getQuantity)
                    .containsExactly(
                            tuple(OrderedProduct.of(1L, 1L, "TEST", "상품", "http://thumbnail.jpg"),
                                    OrderItemPrice.of(10000L, 10, 1000L, 9000L),
                                    9000L, 1)
                    );

            // 주문 상품 옵션
            assertThat(order.getOrderItems())
                    .flatExtracting(OrderItem::getOrderItemOptions)
                    .extracting("optionTypeName", "optionValueName")
                    .containsExactly(
                            tuple("사이즈", "XL")
                    );
        }

        @Test
        @DisplayName("쿠폰을 사용하지 않은 주문을 생성한다")
        void createOrder_not_use_coupon(){
            //given
            OrderCreationContext context = anOrderCreationContext()
                    .coupon(null)
                    .orderPrice(OrderCreationContext.OrderPriceSpec.of(10000L, 1000L, 0L, 1000L, 8000L))
                    .build();
            //when
            Order order = Order.create(context);
            //then
            assertThat(order.getCoupon()).isNull();
            assertThat(order.getOrderPriceDetail())
                    .isEqualTo(OrderPriceDetail.of(10000L, 1000L, 0L, 1000L, 8000L));
        }

        @Test
        @DisplayName("여러개의 주문 상품이 있는 경우 주문 이름은 첫번째 상품 이름 외 1건 이다")
        void createOrder_several_items(){
            //given
            OrderCreationContext context = anOrderCreationContext()
                    .orderItemCreationContexts(
                            List.of(anOrderItemCreationContext().productSpec(anProductSpec().productVariantId(1L).build()).build(),
                                    anOrderItemCreationContext().productSpec(anProductSpec().productVariantId(2L).build()).build()))
                    .orderPrice(anOrderPriceSpec()
                            .totalOriginPrice(20000L)
                            .totalProductDiscount(2000L)
                            .couponDiscount(1000L)
                            .pointDiscount(1000L)
                            .finalPaymentAmount(16000L)
                            .build())
                    .build();
            //when
            Order order = Order.create(context);
            //then
            assertThat(order.getOrderName()).isEqualTo("상품 외 1건");
            assertThat(order.getOrderItems()).hasSize(2);
        }

        @Test
        @DisplayName("주문 상품이 비어있으면 예외를 던진다")
        void createOrder_items_less_than_1(){
            //given
            OrderCreationContext context = anOrderCreationContext().orderItemCreationContexts(List.of()).build();
            //when
            //then
            assertThatThrownBy(() -> Order.create(context))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_ITEM_MINIMUM_ONE_REQUIRED);
        }
    }

    @Nested
    @DisplayName("주문 상태 변경")
    class UpdateStatus {

        @Test
        @DisplayName("주문 상태를 결제 대기로 변경한다")
        void preparePaymentWaiting(){
            //given
            OrderCreationContext context = anOrderCreationContext().build();
            Order order = Order.create(context);
            //when
            order.preparePaymentWaiting();
            //then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_WAITING);
        }

        @Test
        @DisplayName("주문 상태를 결제 실패로 변경한다")
        void paymentFailed(){
            //given
            OrderCreationContext context = anOrderCreationContext().build();
            Order order = Order.create(context);
            //when
            order.paymentFailed(OrderFailureCode.PAYMENT_INSUFFICIENT_BALANCE);
            //then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
            assertThat(order.getFailureCode()).isEqualTo(OrderFailureCode.PAYMENT_INSUFFICIENT_BALANCE);
        }

        @Test
        @DisplayName("주문을 취소한다")
        void canceled(){
            //given
            OrderCreationContext context = anOrderCreationContext().build();
            Order order = Order.create(context);
            //when
            order.canceled(OrderFailureCode.OUT_OF_STOCK);
            //then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
            assertThat(order.getFailureCode()).isEqualTo(OrderFailureCode.OUT_OF_STOCK);
        }
    }
}

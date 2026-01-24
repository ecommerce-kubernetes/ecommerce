package com.example.order_service.api.order.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderTest {

//    @Test
//    @DisplayName("주문 생성시 쿠폰이 있는 경우 쿠폰 정보가 매핑된 주문을 생성한다")
//    void createOrder(){
//        //given
//        OrderCreationContext command = createOrderCommand().build();
//        //when
//        Order order = Order.create(command);
//        //then
//        assertThat(order)
//                .extracting(
//                        Order::getUserId, Order::getStatus, Order::getOrderName, Order::getDeliveryAddress, Order::getFailureCode)
//                .contains(1L, OrderStatus.PENDING, "상품1", "서울시 테헤란로 123", null);
//
//        assertThat(order.getPriceInfo())
//                .extracting(OrderPriceInfo::getTotalOriginPrice, OrderPriceInfo::getTotalProductDiscount, OrderPriceInfo::getCouponDiscount,
//                        OrderPriceInfo::getPointDiscount, OrderPriceInfo::getFinalPaymentAmount)
//                .containsExactly(10000L, 1000L, 1000L, 1000L, 7000L);
//
//        assertThat(order.getCoupon()).isNotNull();
//        assertThat(order.getCoupon().getOrder()).isEqualTo(order);
//        assertThat(order.getCoupon())
//                .extracting("couponId", "couponName", "discountAmount")
//                .contains(1L, "1000원 할인 쿠폰", 1000L);
//
//        assertThat(order.getOrderItems())
//                .allSatisfy(orderItem -> assertThat(orderItem.getOrder()).isEqualTo(order));
//    }
//
//    @Test
//    @DisplayName("주문 생성시 쿠폰이 없는 경우 쿠폰 정보가 매핑되지 않은 주문을 생성한다")
//    void createOrder_Without_Coupon(){
//        //given
//        OrderCreationContext command = createOrderCommand()
//                .couponInfo(null)
//                .orderPriceInfo(OrderPriceInfo.builder()
//                        .totalOriginPrice(10000L)
//                        .totalProductDiscount(1000L)
//                        .couponDiscount(0)
//                        .pointDiscount(1000L)
//                        .finalPaymentAmount(8000L)
//                        .build())
//                .build();
//        //when
//        Order order = Order.create(command);
//        //then
//        assertThat(order)
//                .extracting(
//                        Order::getUserId, Order::getStatus, Order::getOrderName, Order::getDeliveryAddress, Order::getFailureCode)
//                .contains(1L, OrderStatus.PENDING, "상품1", "서울시 테헤란로 123", null);
//
//        assertThat(order.getPriceInfo())
//                .extracting(OrderPriceInfo::getTotalOriginPrice, OrderPriceInfo::getTotalProductDiscount, OrderPriceInfo::getCouponDiscount,
//                        OrderPriceInfo::getPointDiscount, OrderPriceInfo::getFinalPaymentAmount)
//                .containsExactly(10000L, 1000L, 0L, 1000L, 8000L);
//
//        assertThat(order.getCoupon()).isNull();
//
//        assertThat(order.getOrderItems())
//                .allSatisfy(orderItem -> assertThat(orderItem.getOrder()).isEqualTo(order));
//    }
//
//    @Test
//    @DisplayName("주문 상품이 비어있으면 예외를 던진다")
//    void createOrder_items_less_than_1(){
//        //given
//        OrderCreationContext command = createOrderCommand().itemCommands(List.of())
//                .build();
//        //when
//        //then
//        assertThatThrownBy(() -> Order.create(command))
//                .isInstanceOf(BusinessException.class)
//                .extracting("errorCode")
//                .isEqualTo(OrderErrorCode.ORDER_ITEM_MINIMUM_ONE_REQUIRED);
//    }
//
//    @Test
//    @DisplayName("주문의 상태를 변경한다")
//    void changeStatus() {
//        //given
//        OrderCreationContext command = createOrderCommand().build();
//        Order order = Order.create(command);
//        //when
//        order.changeStatus(OrderStatus.PAYMENT_WAITING);
//        //then
//        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_WAITING);
//    }
//
//    @Test
//    @DisplayName("주문을 취소한다")
//    void canceled() {
//        //given
//        OrderCreationContext command = createOrderCommand().build();
//        Order order = Order.create(command);
//        //when
//        order.canceled(OrderFailureCode.OUT_OF_STOCK);
//        //then
//        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
//        assertThat(order.getFailureCode()).isEqualTo(OrderFailureCode.OUT_OF_STOCK);
//    }
//
//    private OrderCreationContext.OrderCreationContextBuilder createOrderCommand() {
//        return OrderCreationContext.builder()
//                .userId(1L)
//                .itemCommands(List.of(createOrderItemCommands()))
//                .orderPriceInfo(createOrderPriceInfo())
//                .couponInfo(createAppliedCoupon())
//                .deliveryAddress("서울시 테헤란로 123");
//    }
//
//    private OrderItemCreationContext createOrderItemCommands() {
//        return OrderItemCreationContext.builder()
//                .productId(1L)
//                .productVariantId(1L)
//                .productName("상품1")
//                .thumbnailUrl("http://thumbnail.jpg")
//                .unitPrice(
//                        OrderItemCreationContext.UnitPrice.builder()
//                                .originalPrice(10000L)
//                                .discountRate(10)
//                                .discountAmount(1000L)
//                                .discountedPrice(9000L)
//                                .build())
//                .itemOptions(List.of())
//                .quantity(1)
//                .lineTotal(9000L)
//                .build();
//    }
//
//    private OrderPriceInfo createOrderPriceInfo() {
//        return OrderPriceInfo.builder()
//                .totalOriginPrice(10000L)
//                .totalProductDiscount(1000L)
//                .couponDiscount(1000L)
//                .pointDiscount(1000L)
//                .finalPaymentAmount(7000L)
//                .build();
//    }
//
//    private CouponInfo createAppliedCoupon(){
//        return CouponInfo.builder()
//                .couponId(1L)
//                .couponName("1000원 할인 쿠폰")
//                .discountAmount(1000L)
//                .build();
//    }
}

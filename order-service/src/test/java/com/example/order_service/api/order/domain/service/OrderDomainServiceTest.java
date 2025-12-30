package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.model.vo.AppliedCoupon;
import com.example.order_service.api.order.domain.model.vo.PaymentInfo;
import com.example.order_service.api.order.domain.repository.OrderRepository;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import com.example.order_service.api.support.ExcludeInfraTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import static com.example.order_service.api.support.fixture.OrderDomainServiceTestFixture.*;
import static org.assertj.core.api.Assertions.*;

@Transactional
public class OrderDomainServiceTest extends ExcludeInfraTest {

    @Autowired
    private OrderDomainService orderDomainService;
    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문을 저장한다")
    void saveOrder(){
        //given
        OrderCreationContext creationContext = createDefaultContext();
        //when
        OrderDto orderDto = orderDomainService.saveOrder(creationContext);
        //then
        assertThat(orderDto.getOrderId()).isNotNull();
        assertThat(orderDto)
                .extracting(OrderDto::getStatus, OrderDto::getOrderName, OrderDto::getOrderFailureCode)
                .contains(OrderStatus.PENDING, "상품1 외 1건", null);
        assertThat(orderDto.getOrderedAt()).isNotNull();

        assertThat(orderDto.getPaymentInfo())
                .extracting(
                        PaymentInfo::getTotalOriginPrice,
                        PaymentInfo::getTotalProductDiscount,
                        PaymentInfo::getCouponDiscount,
                        PaymentInfo::getUsedPoint,
                        PaymentInfo::getFinalPaymentAmount
                )
                .contains(
                        TOTAL_ORIGIN_PRICE,
                        TOTAL_PROD_DISCOUNT,
                        COUPON_DISCOUNT,
                        USE_POINT,
                        FINAL_PRICE
                );

        assertThat(orderDto.getOrderItemDtoList())
                .hasSize(2)
                        .extracting(
                                OrderItemDto::getProductId,
                                OrderItemDto::getProductName,
                                OrderItemDto::getQuantity,
                                OrderItemDto::getLineTotal
                        )
                .containsExactlyInAnyOrder(
                        tuple(PROD1_ID, PROD1_NAME, PROD1_QTY, PROD1_LINE_TOTAL),
                        tuple(PROD2_ID, PROD2_NAME, PROD2_QTY, PROD2_LINE_TOTAL)
                );

        assertThat(orderDto.getAppliedCoupon())
                .extracting(AppliedCoupon::getCouponId, AppliedCoupon::getCouponName, AppliedCoupon::getDiscountAmount)
                .containsExactly(1L, "1000원 할인 쿠폰", COUPON_DISCOUNT);
    }

    @Test
    @DisplayName("주문 상태를 변경한다")
    void changeOrderStatus() {
        //given
        OrderCreationContext context = createDefaultContext();
        Order order = Order.create(context);
        Order savedOrder = orderRepository.save(order);
        //when
        OrderDto result = orderDomainService.changeOrderStatus(savedOrder.getId(), OrderStatus.PAYMENT_WAITING);
        //then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PAYMENT_WAITING);
        assertThat(result.getOrderId()).isEqualTo(savedOrder.getId());
        assertThat(result.getOrderFailureCode()).isNull();
    }

    @Test
    @DisplayName("주문의 상태를 변경할때 주문을 찾을 수 없으면 예외를 던진다")
    void changeOrderStatus_notFound() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderDomainService.changeOrderStatus(999L, OrderStatus.PAYMENT_WAITING))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("주문을 조회한다")
    void getOrder(){
        //given
        OrderCreationContext context = createDefaultContext();
        Order savedOrder = orderRepository.save(Order.create(context));
        //when
        OrderDto result = orderDomainService.getOrder(savedOrder.getId());
        //then
        assertThat(result.getOrderId()).isEqualTo(savedOrder.getId());
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);

        assertThat(result.getPaymentInfo())
                .extracting(PaymentInfo::getTotalOriginPrice, PaymentInfo::getTotalProductDiscount,
                        PaymentInfo::getCouponDiscount, PaymentInfo::getUsedPoint, PaymentInfo::getFinalPaymentAmount)
                .contains(TOTAL_ORIGIN_PRICE, TOTAL_PROD_DISCOUNT, COUPON_DISCOUNT, USE_POINT, FINAL_PRICE);

        assertThat(result.getOrderItemDtoList())
                .hasSize(2)
                        .extracting(
                                OrderItemDto::getProductId,
                                OrderItemDto::getProductName,
                                OrderItemDto::getQuantity,
                                OrderItemDto::getLineTotal
                        )
                        .containsExactly(
                                tuple(PROD1_ID, PROD1_NAME, PROD1_QTY, PROD1_LINE_TOTAL),
                                tuple(PROD2_ID, PROD2_NAME, PROD2_QTY, PROD2_LINE_TOTAL)
                        );

        assertThat(result.getAppliedCoupon())
                .extracting(AppliedCoupon::getCouponId, AppliedCoupon::getCouponName, AppliedCoupon::getDiscountAmount)
                .contains(1L, "1000원 할인 쿠폰", COUPON_DISCOUNT);
    }

    @Test
    @DisplayName("주문을 조회할때 주문을 찾을 수 없으면 예외를 던진다")
    void getOrder_notFound(){
        //given
        //when
        //then
        assertThatThrownBy(() -> orderDomainService.getOrder(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("주문을 실패 상태로 변경한다")
    void changeCanceled() {
        //given
        OrderCreationContext context = createDefaultContext();
        Order savedOrder = orderRepository.save(Order.create(context));
        OrderFailureCode failureCode = OrderFailureCode.OUT_OF_STOCK;
        //when
        OrderDto result = orderDomainService.changeCanceled(savedOrder.getId(), failureCode);
        //then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(result.getOrderFailureCode()).isEqualTo(OrderFailureCode.OUT_OF_STOCK);
        assertThat(result.getOrderId()).isEqualTo(savedOrder.getId());
    }

    @Test
    @DisplayName("주문을 실패 상태로 변경할때 주문을 찾을 수 없으면 예외를 던진다")
    void changeCanceled_notFound() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderDomainService.changeCanceled(999L, OrderFailureCode.OUT_OF_STOCK))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("유저 ID 와 조회 커맨드로 주문 목록을 조회한다")
    void getOrders(){
        //given
        OrderCreationContext context = createDefaultContext();
        Order savedOrder1 = orderRepository.save(Order.create(context));
        Order savedOrder2 = orderRepository.save(Order.create(context));
        OrderSearchCondition condition = OrderSearchCondition.builder()
                .page(1)
                .size(10)
                .sort("latest").build();
        //when
        Page<OrderDto> result = orderDomainService.getOrders(USER_ID, condition);
        //then

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getNumber()).isEqualTo(0);

        assertThat(result.getContent())
                .extracting(
                        OrderDto::getUserId,
                        OrderDto::getStatus,
                        o -> o.getPaymentInfo().getFinalPaymentAmount()
                )
                .contains(
                        tuple(USER_ID, OrderStatus.PENDING, FINAL_PRICE),
                        tuple(USER_ID, OrderStatus.PENDING, FINAL_PRICE)
                );

        assertThat(result.getContent())
                .extracting(OrderDto::getOrderId)
                .containsExactly(savedOrder2.getId(), savedOrder1.getId());
    }
}

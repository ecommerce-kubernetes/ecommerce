package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderItem;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.repository.OrderRepository;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext.OrderPriceSpec;
import com.example.order_service.api.order.domain.service.dto.command.PaymentCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import com.example.order_service.api.support.ExcludeInfraTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.order_service.api.support.fixture.OrderCommandFixture.anOrderSearchCondition;
import static com.example.order_service.api.support.fixture.OrderFixture.*;
import static org.assertj.core.api.Assertions.*;

@Transactional
public class OrderServiceTest extends ExcludeInfraTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;

    @Nested
    @DisplayName("주문 저장")
    class Save {

        @Test
        @DisplayName("쿠폰 사용 주문을 생성한다")
        void saveOrder_use_coupon() {
            //given
            OrderCreationContext creationContext = anOrderCreationContext().build();
            OrderDto expectedResult = returnOrderDto().build();
            //when
            OrderDto result = orderService.saveOrder(creationContext);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .ignoringFields("id", "orderNo", "orderedAt", "orderItems.id")
                    .isEqualTo(expectedResult);

            assertThat(result).satisfies(r -> {
                assertThat(r.getId()).isNotNull();
                assertThat(r.getOrderNo()).isNotNull();
                assertThat(r.getOrderedAt()).isNotNull();
            });
            assertThat(result.getOrderItems()).allSatisfy(i -> assertThat(i.getId()).isNotNull());
        }

        @Test
        @DisplayName("쿠폰을 사용하지 않은 주문을 생성한다")
        void saveOrder_not_use_coupon(){
            //given
            OrderCreationContext context = anOrderCreationContext()
                    .coupon(null)
                    .orderPrice(OrderPriceSpec.of(10000L, 1000L, 0L, 1000L, 8000L))
                    .build();
            OrderDto expectedResult = returnOrderDto()
                    .couponInfo(null)
                    .orderPriceInfo(OrderDto.OrderPriceInfo.builder()
                            .totalOriginPrice(10000L)
                            .totalProductDiscount(1000L)
                            .couponDiscount(0)
                            .pointDiscount(1000L)
                            .finalPaymentAmount(8000L).build())
                    .build();

            //when
            OrderDto result = orderService.saveOrder(context);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .ignoringFields("id", "orderNo", "orderedAt", "orderItems.id")
                    .isEqualTo(expectedResult);

            assertThat(result).satisfies(r -> {
                assertThat(r.getId()).isNotNull();
                assertThat(r.getOrderNo()).isNotNull();
                assertThat(r.getOrderedAt()).isNotNull();
            });

            assertThat(result.getOrderItems()).allSatisfy(i -> assertThat(i.getId()).isNotNull());
        }
    }

    @Nested
    @DisplayName("주문 상태 변경")
    class ChangeStatus {

        @Test
        @DisplayName("주문 상태를 결제 대기로 변경한다")
        void preparePaymentWaiting(){
            //given
            OrderCreationContext context = anOrderCreationContext().build();
            Order savedOrder = orderRepository.save(Order.create(context));
            //when
            OrderDto result = orderService.preparePaymentWaiting(savedOrder.getOrderNo());
            //then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PAYMENT_WAITING);
        }

        @Test
        @DisplayName("주문 상품을 찾을 수 없으면 결제 대기 상태로 변경할 수 없다")
        void preparePaymentWaiting_order_not_found(){
            //given
            //when
            //then
            assertThatThrownBy(() -> orderService.preparePaymentWaiting("UNKNOWN"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
        }

        @Test
        @DisplayName("주문 상태를 취소로 변경한다")
        void canceledOrder(){
            //given
            OrderCreationContext context = anOrderCreationContext().build();
            Order savedOrder = orderRepository.save(Order.create(context));
            //when
            OrderDto result = orderService.canceledOrder(savedOrder.getOrderNo(), OrderFailureCode.OUT_OF_STOCK);
            //then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELED);
        }

        @Test
        @DisplayName("주문 상품을 찾을 수 없으면 주문 취소를 할 수 없다")
        void canceledOrder_order_not_found(){
            //given
            //when
            //then
            assertThatThrownBy(() -> orderService.canceledOrder("UNKNOWN", OrderFailureCode.OUT_OF_STOCK))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
        }

        @Test
        @DisplayName("주문을 결제 실패로 변경한다")
        void failPayment(){
            //given
            OrderCreationContext context = anOrderCreationContext().build();
            Order savedOrder = orderRepository.save(Order.create(context));
            savedOrder.preparePaymentWaiting();
            //when
            OrderDto result = orderService.failPayment(savedOrder.getOrderNo(), OrderFailureCode.PAYMENT_INSUFFICIENT_BALANCE);
            //then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
            assertThat(result.getOrderFailureCode()).isEqualTo(OrderFailureCode.PAYMENT_INSUFFICIENT_BALANCE);
        }
    }

    @Nested
    @DisplayName("주문 조회")
    class GetOrder {

        @Test
        @DisplayName("주문을 조회한다")
        void getOrder() {
            //given
            OrderCreationContext context = anOrderCreationContext().build();
            Order savedOrder = orderRepository.save(Order.create(context));
            OrderDto expectedResult = returnOrderDto().build();
            List<Long> expectedOrderItemIds = savedOrder.getOrderItems().stream().map(OrderItem::getId).toList();
            //when
            OrderDto result = orderService.getOrder(savedOrder.getOrderNo(), 1L);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .ignoringFields("id", "orderNo", "orderedAt", "orderItems.id")
                    .isEqualTo(expectedResult);

            assertThat(result).satisfies(r -> {
                assertThat(r.getId()).isEqualTo(savedOrder.getId());
                assertThat(r.getOrderNo()).isEqualTo(savedOrder.getOrderNo());
                assertThat(r.getOrderedAt()).isEqualTo(savedOrder.getCreatedAt());
            });

            assertThat(result.getOrderItems())
                    .extracting(OrderItemDto::getId)
                    .containsExactlyInAnyOrderElementsOf(expectedOrderItemIds);
        }

        @Test
        @DisplayName("주문을 찾을 수 없으면 예외가 발생한다")
        void getOrder_not_found_order() {
            //given
            //when
            //then
            assertThatThrownBy(() -> orderService.getOrder("UNKNOWN", 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
        }

        @Test
        @DisplayName("주문을 조회할때 주문자 id와 사용자 id가 다른 경우 예외가 발생한다")
        void getOrder_miss_match_userId() {
            //given
            OrderCreationContext context = anOrderCreationContext().build();
            Order savedOrder = orderRepository.save(Order.create(context));
            //when
            //then
            assertThatThrownBy(() -> orderService.getOrder(savedOrder.getOrderNo(), 2L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_NO_PERMISSION);
        }

        @Test
        @DisplayName("주문 목록을 조회한다")
        void getOrders() {
            //given
            OrderSearchCondition condition = anOrderSearchCondition().build();
            OrderCreationContext context = anOrderCreationContext().build();
            Order order1 = orderRepository.save(Order.create(context));
            Order order2 = orderRepository.save(Order.create(context));
            Order order3 = orderRepository.save(Order.create(context));

            List<OrderDto> expectedContent = List.of(
                    returnOrderDto().orderNo(order3.getOrderNo())
                            .build(),
                    returnOrderDto().orderNo(order2.getOrderNo())
                            .build(),
                    returnOrderDto().orderNo(order1.getOrderNo())
                            .build()
            );
            //when
            Page<OrderDto> result = orderService.getOrders(1L, condition);
            //then
            assertThat(result.getContent())
                    .usingRecursiveComparison()
                    .ignoringFields("id", "orderedAt", "orderItems.id")
                    .isEqualTo(expectedContent);

            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getNumber()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("주문 결제 저장")
    class SavePayment {

        @Test
        @DisplayName("결제 정보를 저장한다")
        void completePayment(){
            //given
            OrderCreationContext orderContext = anOrderCreationContext().build();
            Order savedOrder = orderRepository.save(Order.create(orderContext));
            PaymentCreationContext context = anPaymentContext().orderNo(savedOrder.getOrderNo()).build();
            OrderDto expectedResult = returnOrderDto().orderNo(savedOrder.getOrderNo())
                    .status(OrderStatus.COMPLETED).paymentInfo(returnPayment().build()).build();
            //when
            OrderDto result = orderService.completePayment(context);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .ignoringFields("id", "orderedAt", "orderItems.id", "paymentInfo.id", "paymentInfo.approvedAt")
                    .isEqualTo(expectedResult);

            assertThat(result.getPaymentInfo().getPaymentId()).isNotNull();
            assertThat(result.getPaymentInfo().getApprovedAt()).isNotNull();
        }
    }
}

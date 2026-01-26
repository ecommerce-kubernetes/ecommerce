package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderItem;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.repository.OrderRepository;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext.OrderPriceSpec;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import com.example.order_service.api.support.ExcludeInfraTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        @DisplayName("주문 상태를 변경한다")
        void changeOrderStatus(){
            //given
            OrderCreationContext context = anOrderCreationContext().build();
            Order savedOrder = orderRepository.save(Order.create(context));
            //when
            OrderDto result = orderService.changeOrderStatus(savedOrder.getOrderNo(), OrderStatus.PAYMENT_WAITING);
            //then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PAYMENT_WAITING);
        }

        @Test
        @DisplayName("주문 상품을 찾을 수 없으면 상태를 변경할 수 없다")
        void changeOrderStatus_order_not_found(){
            //given
            //when
            //then
            assertThatThrownBy(() -> orderService.changeOrderStatus("UNKNOWN", OrderStatus.PAYMENT_WAITING))
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
    }
//
//
//    @Test
//    @DisplayName("유저 ID 와 조회 커맨드로 주문 목록을 조회한다")
//    void getOrders(){
//        //given
//        OrderCreationContext context = createDefaultContext();
//        Order savedOrder1 = orderRepository.save(Order.create(context));
//        Order savedOrder2 = orderRepository.save(Order.create(context));
//        OrderSearchCondition condition = OrderSearchCondition.builder()
//                .page(1)
//                .size(10)
//                .sort("latest").build();
//        //when
//        Page<OrderDto> result = orderService.getOrders(USER_ID, condition);
//        //then
//
//        assertThat(result.getTotalElements()).isEqualTo(2);
//        assertThat(result.getTotalPages()).isEqualTo(1);
//        assertThat(result.getContent()).hasSize(2);
//        assertThat(result.getNumber()).isEqualTo(0);
//
//        assertThat(result.getContent())
//                .extracting(
//                        OrderDto::getUserId,
//                        OrderDto::getStatus,
//                        o -> o.getOrderPriceDetail().getFinalPaymentAmount()
//                )
//                .contains(
//                        tuple(USER_ID, OrderStatus.PENDING, FINAL_PRICE),
//                        tuple(USER_ID, OrderStatus.PENDING, FINAL_PRICE)
//                );
//
//        assertThat(result.getContent())
//                .extracting(OrderDto::getOrderId)
//                .containsExactly(savedOrder2.getId(), savedOrder1.getId());
//    }
//
//    @Test
//    @DisplayName("주문에 결제 정보를 저장한다")
//    void completedOrder(){
//        //given
//        String paymentKey = "paymentKey";
//        OrderCreationContext context = createDefaultContext();
//        Order order = Order.create(context);
//        order.changeStatus(OrderStatus.PAYMENT_WAITING);
//        Order savedOrder = orderRepository.save(order);
//        PaymentCreationCommand command = PaymentCreationCommand.builder()
//                .orderNo(savedOrder.getOrderNo())
//                .paymentKey(paymentKey)
//                .amount(order.getPriceInfo().getFinalPaymentAmount())
//                .method("CARD")
//                .approvedAt(LocalDateTime.now())
//                .build();
//        //when
//        OrderDto orderDto = orderService.completedOrder(command);
//        //then
//        assertThat(orderDto.getStatus()).isEqualTo(OrderStatus.COMPLETED);
//        assertThat(orderDto.getPaymentInfo().getId()).isNotNull();
//        assertThat(orderDto.getPaymentInfo())
//                .extracting(PaymentInfo::getPaymentKey, PaymentInfo::getAmount, PaymentInfo::getMethod)
//                .containsExactly(paymentKey, order.getPriceInfo().getFinalPaymentAmount(), "CARD");
//    }
}

package com.example.order_service.api.order.facade;

import com.example.order_service.api.common.dto.PageDto;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.common.exception.PaymentErrorCode;
import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.service.*;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.PaymentCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.*;
import com.example.order_service.api.order.facade.dto.command.CreateOrderCommand;
import com.example.order_service.api.order.facade.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.facade.dto.result.OrderDetailResponse;
import com.example.order_service.api.order.facade.dto.result.OrderListResponse;
import com.example.order_service.api.order.facade.event.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.order_service.api.support.fixture.OrderCommandFixture.*;
import static com.example.order_service.api.support.fixture.OrderCouponFixture.anOrderCouponInfo;
import static com.example.order_service.api.support.fixture.OrderFixture.*;
import static com.example.order_service.api.support.fixture.OrderFixture.ORDER_NO;
import static com.example.order_service.api.support.fixture.OrderPaymentFixture.anOrderPaymentInfo;
import static com.example.order_service.api.support.fixture.OrderPriceFixture.anCalculatedOrderAmounts;
import static com.example.order_service.api.support.fixture.OrderPriceFixture.anOrderProductAmount;
import static com.example.order_service.api.support.fixture.OrderProductFixture.anOrderProductInfo;
import static com.example.order_service.api.support.fixture.OrderResponseFixture.*;
import static com.example.order_service.api.support.fixture.OrderUserFixture.anOrderUserInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OrderFacadeTest {

    @InjectMocks
    private OrderFacade orderFacade;
    @Mock
    private OrderProductService orderProductService;
    @Mock
    private OrderUserService orderUserService;
    @Mock
    private OrderCouponService orderCouponService;
    @Mock
    private OrderPaymentService orderPaymentService;
    @Mock
    private OrderService orderService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private OrderCreationContextMapper mapper;
    @Mock
    private OrderPriceCalculator calculator;

    @Captor
    private ArgumentCaptor<OrderCreatedEvent> orderCreatedEventCaptor;
    @Captor
    private ArgumentCaptor<OrderPaymentReadyEvent> orderPaymentReadyEventCaptor;
    @Captor
    private ArgumentCaptor<OrderFailedEvent> orderFailedEventCaptor;
    @Captor
    private ArgumentCaptor<PaymentFailedEvent> paymentFailedEventCaptor;

    @Nested
    @DisplayName("주문을 생성")
    class InitialOrder {

        @Test
        @DisplayName("중복된 상품은 주문할 수 없다")
        void initialOrder_duplicate_item(){
            //given
            CreateOrderCommand command = anOrderCommand()
                    .orderItemCommands(List.of(anOrderItemCommand().build(), anOrderItemCommand().build()))
                    .build();
            //when
            //then
            assertThatThrownBy(() -> orderFacade.initialOrder(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_DUPLICATE_ORDER_PRODUCT);
        }

        @Test
        @DisplayName("쿠폰을 사용한 주문을 생성하면 쿠폰 아이디가 포함된 주문 생성 이벤트가 발행된다")
        void initialOrder_use_coupon(){
            //given
            OrderDto dto = returnOrderDto()
                    .orderedAt(LocalDateTime.now()).build();
            CreateOrderCommand command = anOrderCommand()
                    .orderItemCommands(List.of(anOrderItemCommand().productVariantId(1L).build()))
                    .build();
            given(orderUserService.getUser(anyLong(), anyLong())).willReturn(anOrderUserInfo().build());
            given(orderProductService.getProducts(anyList())).willReturn(List.of(anOrderProductInfo().build()));
            given(calculator.calculateItemAmounts(anyList(), anyList())).willReturn(anOrderProductAmount().build());
            given(orderCouponService.calculateCouponDiscount(anyLong(), anyLong(), any(OrderProductAmount.class))).willReturn(anOrderCouponInfo().build());
            given(calculator.calculateOrderPrice(any(OrderProductAmount.class), any(OrderCouponInfo.class), anyLong(), anyLong())).willReturn(anCalculatedOrderAmounts().build());
            given(mapper.mapOrderCreationContext(any(OrderUserInfo.class), any(CalculatedOrderAmounts.class), any(OrderCouponInfo.class), any(CreateOrderCommand.class),
                    anyList())).willReturn(anOrderCreationContext().build());
            given(orderService.saveOrder(any(OrderCreationContext.class))).willReturn(dto);

            CreateOrderResponse expectedResult = anCreateOrderResponse().build();
            //when
            CreateOrderResponse result = orderFacade.initialOrder(command);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .ignoringFields("createdAt")
                            .isEqualTo(expectedResult);
            assertThat(result.getCreatedAt()).isNotNull();

            verify(eventPublisher).publishEvent(orderCreatedEventCaptor.capture());
            assertThat(orderCreatedEventCaptor.getValue())
                    .extracting(OrderCreatedEvent::getOrderNo, OrderCreatedEvent::getUserId, OrderCreatedEvent::getCouponId, OrderCreatedEvent::getUsedPoint)
                    .containsExactly(ORDER_NO, 1L, 1L, 1000L);
        }

        @Test
        @DisplayName("쿠폰을 사용하지 않은 주문을 생성하면 쿠폰 아이디가 null인 이벤트가 발행된다")
        void initialOrder_non_use_coupon(){
            //given
            OrderDto dto = returnOrderDto()
                    .couponInfo(null)
                    .orderedAt(LocalDateTime.now()).build();
            CreateOrderCommand command = anOrderCommand()
                    .orderItemCommands(List.of(anOrderItemCommand().productVariantId(1L).build()))
                    .build();
            given(orderUserService.getUser(anyLong(), anyLong())).willReturn(anOrderUserInfo().build());
            given(orderProductService.getProducts(anyList())).willReturn(List.of(anOrderProductInfo().build()));
            given(calculator.calculateItemAmounts(anyList(), anyList())).willReturn(anOrderProductAmount().build());
            given(orderCouponService.calculateCouponDiscount(anyLong(), anyLong(), any(OrderProductAmount.class))).willReturn(anOrderCouponInfo().build());
            given(calculator.calculateOrderPrice(any(OrderProductAmount.class), any(OrderCouponInfo.class), anyLong(), anyLong())).willReturn(anCalculatedOrderAmounts().build());
            given(mapper.mapOrderCreationContext(any(OrderUserInfo.class), any(CalculatedOrderAmounts.class), any(OrderCouponInfo.class), any(CreateOrderCommand.class),
                    anyList())).willReturn(anOrderCreationContext().build());
            given(orderService.saveOrder(any(OrderCreationContext.class))).willReturn(dto);
            CreateOrderResponse expectedResult = anCreateOrderResponse().build();
            //when
            CreateOrderResponse result = orderFacade.initialOrder(command);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                            .ignoringFields("createdAt")
                                    .isEqualTo(expectedResult);

            verify(eventPublisher).publishEvent(orderCreatedEventCaptor.capture());
            assertThat(orderCreatedEventCaptor.getValue())
                    .extracting(OrderCreatedEvent::getOrderNo, OrderCreatedEvent::getUserId, OrderCreatedEvent::getCouponId, OrderCreatedEvent::getUsedPoint)
                    .containsExactly(ORDER_NO, 1L, null, 1000L);
        }
    }

    @Nested
    @DisplayName("주문 상태 변경")
    class ChangeOrderStatus {

        @Test
        @DisplayName("주문 상태를 결제 대기로 변경하고 결제 대기 이벤트를 발행한다")
        void preparePayment(){
            //given
            OrderDto orderDto = returnOrderDto().status(OrderStatus.PAYMENT_WAITING).build();
            given(orderService.preparePaymentWaiting(ORDER_NO)).willReturn(orderDto);
            //when
            orderFacade.preparePayment(ORDER_NO);
            //then
            verify(eventPublisher).publishEvent(orderPaymentReadyEventCaptor.capture());
            assertThat(orderPaymentReadyEventCaptor.getValue())
                    .extracting(OrderPaymentReadyEvent::getOrderNo, OrderPaymentReadyEvent::getUserId, OrderPaymentReadyEvent::getCode,
                            OrderPaymentReadyEvent::getOrderName, OrderPaymentReadyEvent::getFinalPaymentAmount)
                    .containsExactly(ORDER_NO, 1L, "PAYMENT_WAITING", "상품", 7000L);
        }

        @Test
        @DisplayName("주문 상태를 취소로 변경하고 주문 취소 이벤트를 발행한다")
        void processOrderFailure(){
            //given
            OrderDto orderDto = returnOrderDto().status(OrderStatus.CANCELED).orderFailureCode(OrderFailureCode.INSUFFICIENT_STOCK).build();
            given(orderService.canceledOrder(anyString(), any(OrderFailureCode.class))).willReturn(orderDto);
            //when
            orderFacade.processOrderFailure(ORDER_NO, OrderFailureCode.INSUFFICIENT_STOCK);
            //then
            verify(eventPublisher).publishEvent(orderFailedEventCaptor.capture());
            assertThat(orderFailedEventCaptor.getValue())
                    .extracting(OrderFailedEvent::getOrderNo, OrderFailedEvent::getUserId, OrderFailedEvent::getCode,
                            OrderFailedEvent::getOrderName)
                    .containsExactly(ORDER_NO, 1L, "INSUFFICIENT_STOCK", "상품");
        }
    }

    @Nested
    @DisplayName("주문 결제 승인")
    class ConfirmOrderPayment {

        @Test
        @DisplayName("주문 상태가 결제 대기 상태가 아니면 주문 결제 승인을 할 수 없다")
        void confirmOrderPayment_order_status_not_payment_waiting() {
            //given
            OrderDto orderDto = returnOrderDto()
                    .status(OrderStatus.PENDING).build();
            given(orderService.getOrder(anyString(), anyLong()))
                    .willReturn(orderDto);
            //when
            //then
            assertThatThrownBy(() -> orderFacade.confirmOrderPayment(ORDER_NO, 1L, "paymentKey", 7000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_NOT_PAYABLE);
        }

        @Test
        @DisplayName("결제 승인 가격이 주문 최종 가격과 다르면 결제 승인할 수 없다")
        void confirmOrderPayment_amount_missMatch_order_finalPaymentAmount() {
            //given
            OrderDto orderDto = returnOrderDto()
                    .orderNo(ORDER_NO)
                    .status(OrderStatus.PAYMENT_WAITING).build();
            given(orderService.getOrder(anyString(), anyLong()))
                    .willReturn(orderDto);
            //when
            //then
            assertThatThrownBy(() -> orderFacade.confirmOrderPayment(ORDER_NO, 1L, "paymentKey", 8000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_PRICE_MISMATCH);
        }

        @Test
        @DisplayName("결제가 승인되면 주문상태를 성공으로 변경, 결제 승인 이벤트를 발행하고 응답을 반환한다")
        void confirmOrderPayment(){
            //given
            OrderDto getOrderDto = returnOrderDto().status(OrderStatus.PAYMENT_WAITING).build();
            OrderDto paymentResultDto = returnOrderDto().status(OrderStatus.COMPLETED).paymentInfo(returnPayment().build()).build();
            OrderPaymentInfo orderPaymentInfo = anOrderPaymentInfo().build();
            PaymentCreationContext paymentContext = anPaymentContext().build();
            given(orderService.getOrder(anyString(), anyLong()))
                    .willReturn(getOrderDto);
            given(orderPaymentService.confirmOrderPayment(anyString(), anyString(), anyLong()))
                    .willReturn(orderPaymentInfo);
            given(mapper.mapPaymentCreationContext(any(OrderPaymentInfo.class)))
                    .willReturn(paymentContext);
            given(orderService.completePayment(any(PaymentCreationContext.class)))
                    .willReturn(paymentResultDto);

            OrderDetailResponse expectedResult = anOrderDetailResponse().build();
            //when
            OrderDetailResponse result = orderFacade.confirmOrderPayment(ORDER_NO, 1L, "paymentKey", 7000L);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .ignoringFields("createdAt", "payment.approvedAt")
                    .isEqualTo(expectedResult);
        }

        @Test
        @DisplayName("결제 승인시 결제 서비스에서 에러가 발생하면 주문을 실패로 처리하고 결제 실패 이벤트를 발행한다")
        void confirmOrderPayment_orderPaymentService_throw_exception(){
            //given
            OrderDto getOrderDto = returnOrderDto().status(OrderStatus.PAYMENT_WAITING).build();
            given(orderService.getOrder(anyString(), anyLong()))
                    .willReturn(getOrderDto);
            OrderDto failOrderDto = returnOrderDto().status(OrderStatus.CANCELED).build();
            willThrow(new BusinessException(PaymentErrorCode.PAYMENT_INSUFFICIENT_BALANCE)).given(orderPaymentService)
                    .confirmOrderPayment(anyString(), anyString(), anyLong());
            given(orderService.failPayment(anyString(), any(OrderFailureCode.class))).willReturn(failOrderDto);
            //when
            //then
            assertThatThrownBy(() -> orderFacade.confirmOrderPayment(ORDER_NO, 1L, "paymentKey", 7000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_INSUFFICIENT_BALANCE);

            verify(orderService, times(1)).failPayment(anyString(), any(OrderFailureCode.class));
            verify(eventPublisher).publishEvent(paymentFailedEventCaptor.capture());
            assertThat(paymentFailedEventCaptor.getValue())
                    .extracting(PaymentFailedEvent::getOrderNo, PaymentFailedEvent::getUserId, PaymentFailedEvent::getCode, PaymentFailedEvent::getFailureReason)
                    .containsExactlyInAnyOrder(ORDER_NO, 1L, "PAYMENT_INSUFFICIENT_BALANCE", "잔액이 부족합니다");
        }
    }

    @Nested
    @DisplayName("주문 조회")
    class GetOrder {

        @Test
        @DisplayName("주문을 조회한다")
        void getOrder(){
            //given
            OrderDto orderDto = returnOrderDto().status(OrderStatus.COMPLETED)
                    .paymentInfo(returnPayment().build())
                    .build();
            given(orderService.getOrder(anyString(), anyLong()))
                    .willReturn(orderDto);
            OrderDetailResponse expectedResult = anOrderDetailResponse().build();
            //when
            OrderDetailResponse result = orderFacade.getOrder(1L, ORDER_NO);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .ignoringFields("createdAt", "payment.approvedAt")
                    .isEqualTo(expectedResult);

            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getPayment().getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("쿠폰을 사용하지 않은 주문을 조회한다")
        void getOrder_no_coupon() {
            //given
            OrderDto orderDto = returnOrderDto().status(OrderStatus.COMPLETED)
                    .couponInfo(null)
                    .orderPriceInfo(returnOrderPrice().couponDiscount(0).build())
                    .paymentInfo(returnPayment().build())
                    .build();
            given(orderService.getOrder(anyString(), anyLong()))
                    .willReturn(orderDto);
            OrderDetailResponse expectedResult = anOrderDetailResponse()
                    .orderPrice(anOrderPriceResponse().couponDiscount(0L).build())
                    .couponResponse(null)
                    .build();
            //when
            OrderDetailResponse result = orderFacade.getOrder(1L, ORDER_NO);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .ignoringFields("createdAt", "payment.approvedAt")
                    .isEqualTo(expectedResult);

            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getPayment().getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("결제 정보가 없는 주문을 조회한다")
        void getOrder_no_payment() {
            //given
            OrderDto orderDto = returnOrderDto().status(OrderStatus.PAYMENT_WAITING)
                    .paymentInfo(null)
                    .build();
            given(orderService.getOrder(anyString(), anyLong()))
                    .willReturn(orderDto);
            OrderDetailResponse expectedResult = anOrderDetailResponse().status("PAYMENT_WAITING")
                    .payment(null)
                    .build();
            //when
            OrderDetailResponse result = orderFacade.getOrder(1L, ORDER_NO);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .ignoringFields("createdAt")
                    .isEqualTo(expectedResult);
        }
    }

    @Nested
    @DisplayName("주문 목록 조회")
    class GetOrders {

        @Test
        @DisplayName("주문 목록을 조회한다")
        void getOrders() {
            //given
            OrderSearchCondition condition = anOrderSearchCondition().build();
            OrderDto order1 = returnOrderDto()
                    .orderNo(ORDER_NO + "1")
                    .status(OrderStatus.COMPLETED)
                    .orderItems(List.of(
                            returnOrderItem().orderedProduct(returnOrderItemProduct().productVariantId(1L).build()).build()
                    )).build();
            OrderDto order2 = returnOrderDto()
                    .orderNo(ORDER_NO + "2")
                    .status(OrderStatus.COMPLETED)
                    .orderItems(List.of(
                            returnOrderItem().orderedProduct(returnOrderItemProduct().productVariantId(2L).build()).build()
                    )).build();
            Page<OrderDto> returnOrders = new PageImpl<>(List.of(order1, order2), PageRequest.of(0, 10), 2);
            given(orderService.getOrders(anyLong(), any(OrderSearchCondition.class)))
                    .willReturn(returnOrders);

            OrderListResponse expectedOrder1 = anOrderListResponse().orderStatus("COMPLETED")
                    .orderNo(ORDER_NO + "1")
                    .orderItems(List.of(anOrderItemResponse().productVariantId(1L).build())).build();

            OrderListResponse expectedOrder2 = anOrderListResponse().orderStatus("COMPLETED")
                    .orderNo(ORDER_NO + "2")
                    .orderItems(List.of(anOrderItemResponse().productVariantId(2L).build())).build();

            PageDto<OrderListResponse> expectedResult = anOrderListPageResponse()
                    .content(List.of(expectedOrder1, expectedOrder2))
                    .currentPage(1)
                    .pageSize(10)
                    .totalElement(2)
                    .totalPage(1)
                    .build();
            //when
            PageDto<OrderListResponse> result = orderFacade.getOrders(1L, condition);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .ignoringFields("content.createdAt")
                    .isEqualTo(expectedResult);
        }
    }
}

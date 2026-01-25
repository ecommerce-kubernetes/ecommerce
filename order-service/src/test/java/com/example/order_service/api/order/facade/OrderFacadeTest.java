package com.example.order_service.api.order.facade;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.domain.service.*;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.*;
import com.example.order_service.api.order.facade.dto.command.CreateOrderCommand;
import com.example.order_service.api.order.facade.dto.command.CreateOrderItemCommand;
import com.example.order_service.api.order.facade.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.facade.event.OrderCreatedEvent;
import com.example.order_service.api.order.facade.event.OrderFailedEvent;
import com.example.order_service.api.order.facade.event.OrderPaymentReadyEvent;
import com.example.order_service.api.order.facade.event.PaymentResultEvent;
import com.example.order_service.api.support.fixture.OrderCommandFixture;
import com.example.order_service.api.support.fixture.OrderFixture;
import com.example.order_service.api.support.fixture.OrderUserFixture;
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

import java.time.LocalDateTime;
import java.util.List;

import static com.example.order_service.api.support.fixture.OrderCommandFixture.anOrderCommand;
import static com.example.order_service.api.support.fixture.OrderCommandFixture.anOrderItemCommand;
import static com.example.order_service.api.support.fixture.OrderCouponFixture.anOrderCouponInfo;
import static com.example.order_service.api.support.fixture.OrderUserFixture.anOrderUserInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
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
    private ArgumentCaptor<PaymentResultEvent> paymentResultEventCaptor;

    private static final String ORDER_NO = "ORDER-20261149-sXvczFv";

    private CreateOrderItemCommand mockOrderItemCommand(Long variantId, int quantity) {
        return CreateOrderItemCommand.builder()
                .productVariantId(variantId)
                .quantity(quantity)
                .build();
    }

    private CreateOrderCommand mockOrderCommand(List<CreateOrderItemCommand> items) {
        return CreateOrderCommand.builder()
                .userId(1L)
                .orderItemCommands(items)
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .pointToUse(1000L)
                .expectedPrice(117000L)
                .build();
    }

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
            OrderDto dto = OrderFixture.returnOrderDto()
                    .orderNo(ORDER_NO)
                    .orderedAt(LocalDateTime.now()).build();
            CreateOrderCommand command = anOrderCommand()
                    .orderItemCommands(List.of(anOrderItemCommand().productVariantId(1L).build(), anOrderItemCommand().productVariantId(2L).build()))
                    .build();
            given(orderUserService.getUser(anyLong(), anyLong())).willReturn(anOrderUserInfo().build());
            given(orderProductService.getProducts(anyList())).willReturn(List.of(OrderProductInfo.builder().build()));
            given(calculator.calculateItemAmounts(anyList(), anyList())).willReturn(OrderProductAmount.builder().build());
            given(orderCouponService.calculateCouponDiscount(anyLong(), anyLong(), any(OrderProductAmount.class))).willReturn(anOrderCouponInfo().build());
            given(calculator.calculateOrderPrice(any(OrderProductAmount.class), any(OrderCouponInfo.class), anyLong(), anyLong())).willReturn(CalculatedOrderAmounts.builder().build());
            given(mapper.mapOrderCreationContext(any(OrderUserInfo.class), any(CalculatedOrderAmounts.class), any(OrderCouponInfo.class), any(CreateOrderCommand.class),
                    anyList())).willReturn(OrderCreationContext.builder().build());
            given(orderService.saveOrder(any(OrderCreationContext.class))).willReturn(dto);
            //when
            CreateOrderResponse result = orderFacade.initialOrder(command);
            //then
            assertThat(result)
                    .extracting(CreateOrderResponse::getOrderNo, CreateOrderResponse::getStatus, CreateOrderResponse::getOrderName,
                            CreateOrderResponse::getFinalPaymentAmount)
                    .containsExactly(ORDER_NO, "PENDING", "상품", 7000L);

            verify(eventPublisher).publishEvent(orderCreatedEventCaptor.capture());
            assertThat(orderCreatedEventCaptor.getValue())
                    .extracting(OrderCreatedEvent::getOrderNo, OrderCreatedEvent::getUserId, OrderCreatedEvent::getCouponId, OrderCreatedEvent::getUsedPoint)
                    .containsExactly(ORDER_NO, 1L, 1L, 1000L);
        }

        @Test
        @DisplayName("쿠폰을 사용하지 않은 주문을 생성하면 쿠폰 아이디가 null인 이벤트가 발행된다")
        void initialOrder_non_use_coupon(){
            //given
            OrderDto dto = OrderFixture.returnOrderDto()
                    .orderNo(ORDER_NO)
                    .couponInfo(null)
                    .orderedAt(LocalDateTime.now()).build();
            CreateOrderCommand command = mockOrderCommand(List.of(mockOrderItemCommand(1L, 3), mockOrderItemCommand(2L, 5)));
            given(orderUserService.getUser(anyLong(), anyLong())).willReturn(OrderUserInfo.builder().build());
            given(orderProductService.getProducts(anyList())).willReturn(List.of(OrderProductInfo.builder().build()));
            given(calculator.calculateItemAmounts(anyList(), anyList())).willReturn(OrderProductAmount.builder().build());
            given(orderCouponService.calculateCouponDiscount(anyLong(), anyLong(), any(OrderProductAmount.class))).willReturn(anOrderCouponInfo().build());
            given(calculator.calculateOrderPrice(any(OrderProductAmount.class), any(OrderCouponInfo.class), anyLong(), anyLong())).willReturn(CalculatedOrderAmounts.builder().build());
            given(mapper.mapOrderCreationContext(any(OrderUserInfo.class), any(CalculatedOrderAmounts.class), any(OrderCouponInfo.class), any(CreateOrderCommand.class),
                    anyList())).willReturn(OrderCreationContext.builder().build());
            given(orderService.saveOrder(any(OrderCreationContext.class))).willReturn(dto);
            //when
            CreateOrderResponse result = orderFacade.initialOrder(command);
            //then
            assertThat(result)
                    .extracting(CreateOrderResponse::getOrderNo, CreateOrderResponse::getStatus, CreateOrderResponse::getOrderName,
                            CreateOrderResponse::getFinalPaymentAmount)
                    .containsExactly(ORDER_NO, "PENDING", "상품", 7000L);

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
//            OrderDto orderDto = mockOrderDto(OrderStatus.PAYMENT_WAITING, null, null);
//            given(orderService.changeOrderStatus(ORDER_NO, OrderStatus.PAYMENT_WAITING)).willReturn(orderDto);
//            //when
//            orderFacade.preparePayment(ORDER_NO);
//            //then
//            verify(eventPublisher).publishEvent(orderPaymentReadyEventCaptor.capture());
//            assertThat(orderPaymentReadyEventCaptor.getValue())
//                    .extracting(OrderPaymentReadyEvent::getOrderNo, OrderPaymentReadyEvent::getUserId, OrderPaymentReadyEvent::getCode,
//                            OrderPaymentReadyEvent::getOrderName, OrderPaymentReadyEvent::getFinalPaymentAmount)
//                    .containsExactly(ORDER_NO, 1L, "PAYMENT_WAITING", "상품 외 1건", 117000L);
        }

        @Test
        @DisplayName("주문 상태를 취소로 변경하고 주문 취소 이벤트를 발행한다")
        void processOrderFailure(){
            //given
//            OrderDto orderDto = mockOrderDto(OrderStatus.CANCELED, null, OrderFailureCode.OUT_OF_STOCK);
//            given(orderService.canceledOrder(ORDER_NO, OrderFailureCode.OUT_OF_STOCK)).willReturn(orderDto);
//            //when
//            orderFacade.processOrderFailure(ORDER_NO, OrderFailureCode.OUT_OF_STOCK);
//            //then
//            verify(eventPublisher).publishEvent(orderFailedEventCaptor.capture());
//            assertThat(orderFailedEventCaptor.getValue())
//                    .extracting(OrderFailedEvent::getOrderNo, OrderFailedEvent::getUserId, OrderFailedEvent::getCode,
//                            OrderFailedEvent::getOrderName)
//                    .containsExactly(ORDER_NO, 1L, "OUT_OF_STOCK", "상품 외 1건");
        }
    }

//    @Test
//    @DisplayName("결제가 승인되면 주문상태를 성공으로 변경, 결제 승인 이벤트를 발행하고 응답을 반환한다")
//    void finalizeOrder(){
//        //given
//        String paymentKey = "paymentKey";
//        Long amount = 28600L;
//        OrderDto waitingOrder = mockSavedOrder(OrderStatus.PAYMENT_WAITING, amount);
//        TossPaymentConfirmResponse paymentResponse = mockPaymentResponse(paymentKey, amount);
//        OrderDto completedOrder = mockSavedOrder(OrderStatus.COMPLETED, amount);
//        given(orderDomainService.getOrder(ORDER_NO, USER_ID))
//                .willReturn(waitingOrder);
//        given(orderExternalAdaptor.confirmOrderPayment(anyString(), anyString(), anyLong()))
//                .willReturn(paymentResponse);
//        given(orderDomainService.completedOrder(any(PaymentCreationCommand.class)))
//                .willReturn(completedOrder);
//        //when
//        OrderDetailResponse result = orderFacade.finalizeOrder(ORDER_NO, USER_ID, paymentKey, amount);
//        //then
//        assertThat(result)
//                .extracting(OrderDetailResponse::getOrderNo, OrderDetailResponse::getUserId, OrderDetailResponse::getOrderStatus, OrderDetailResponse::getOrderName,
//                        OrderDetailResponse::getDeliveryAddress)
//                .containsExactly(ORDER_NO, USER_ID, "COMPLETED", "상품1 외 1건", ADDRESS);
//        assertThat(result.getOrderItems()).isNotEmpty();
//
//        verify(eventPublisher, times(1)).publishEvent(paymentResultEventCaptor.capture());
//
//        assertThat(paymentResultEventCaptor.getValue())
//                .extracting(PaymentResultEvent::getOrderNo, PaymentResultEvent::getStatus, PaymentResultEvent::getCode)
//                .containsExactly(ORDER_NO, OrderEventStatus.SUCCESS, null);
//    }

//    @Test
//    @DisplayName("결제를 승인할때 주문 상태가 결제 대기 상태가 아니면 예외를 던진다")
//    void finalizeOrder_with_notPaymentWaiting(){
//        //given
//        OrderDto invalidStatusOrder = mockSavedOrder(OrderStatus.PENDING, FIXED_FINAL_PRICE);
//        given(orderService.getOrder(anyString(), anyLong()))
//                .willReturn(invalidStatusOrder);
//        //when
//        //then
//        assertThatThrownBy(() -> orderFacade.finalizeOrder(ORDER_NO, USER_ID, "paymentKey", FIXED_FINAL_PRICE))
//                .isInstanceOf(BusinessException.class)
//                .extracting("errorCode")
//                .isEqualTo(OrderErrorCode.ORDER_NOT_PAYABLE);
//    }

//    @Test
//    @DisplayName("결제를 승인할때 요청의 amount 와 실제 최종 주문 금액이 다르면 예외를 던진다")
//    void finalizeOrder_with_missMatch_Price(){
//        //given
//        Long requestedAmount = 30000L;
//        OrderDto orderDto = mockSavedOrder(OrderStatus.PAYMENT_WAITING, FIXED_FINAL_PRICE);
//        given(orderService.getOrder(anyString(), anyLong()))
//                .willReturn(orderDto);
//        //when
//        //then
//        assertThatThrownBy(() -> orderFacade.finalizeOrder(ORDER_NO, USER_ID, "paymentKey", requestedAmount))
//                .isInstanceOf(BusinessException.class)
//                .extracting("errorCode")
//                .isEqualTo(OrderErrorCode.ORDER_PRICE_MISMATCH);
//    }

//    @Test
//    @DisplayName("결제를 승인할때 결제 승인이 실패한 경우 주문을 실패 처리, 주문 실패 이벤트를 발행하고 예외를 그대로 던진다")
//    void finalizeOrder_when_payment_fail(){
//        //given
//        String paymentKey = "paymentKey";
//        String failureMessage = "결제 승인이 거절되었습니다";
//
//        OrderDto waitingOrder = mockSavedOrder(OrderStatus.PAYMENT_WAITING, FIXED_FINAL_PRICE);
//        OrderDto failureOrder = mockCanceledOrder(OrderFailureCode.PAYMENT_FAILED);
//        given(orderDomainService.getOrder(anyString(), anyLong()))
//                .willReturn(waitingOrder);
//        willThrow(new BusinessException(PaymentErrorCode.PAYMENT_APPROVAL_FAIL))
//                .given(orderExternalAdaptor).confirmOrderPayment(anyString(), anyString(), anyLong());
//        given(orderDomainService.canceledOrder(anyString(), any(OrderFailureCode.class)))
//                .willReturn(failureOrder);
//        //when
//        //then
//        assertThatThrownBy(() -> orderFacade.finalizeOrder(ORDER_NO, USER_ID, paymentKey, FIXED_FINAL_PRICE))
//                .isInstanceOf(BusinessException.class)
//                .hasMessage(failureMessage);
//
//        verify(orderDomainService, times(1)).canceledOrder(ORDER_NO, OrderFailureCode.PAYMENT_FAILED);
//
//        verify(eventPublisher, times(1)).publishEvent(paymentResultEventCaptor.capture());
//        assertThat(paymentResultEventCaptor.getValue())
//                .extracting(PaymentResultEvent::getOrderNo, PaymentResultEvent::getStatus, PaymentResultEvent::getCode)
//                .containsExactly(ORDER_NO, OrderEventStatus.FAILURE, OrderFailureCode.PAYMENT_FAILED);
//    }

//    @Test
//    @DisplayName("결제 승인 요청시 결제는 승인되었지만 주문 상태 변경이 실패한 경우 주문을 실패 처리, SAGA 보상을 진행하고 예외를 던진다")
//    void finalizeOrder_when_DB_Exception(){
//        //given
//        String paymentKey = "paymentKey";
//        OrderDto waitingOrder = mockSavedOrder(OrderStatus.PAYMENT_WAITING, FIXED_FINAL_PRICE);
//        OrderDto canceledOrder = mockSavedOrder(OrderStatus.CANCELED, FIXED_FINAL_PRICE);
//        given(orderDomainService.getOrder(anyString(), anyLong()))
//                .willReturn(waitingOrder);
//        TossPaymentConfirmResponse paymentResponse = mockPaymentResponse(paymentKey, FIXED_FINAL_PRICE);
//        given(orderExternalAdaptor.confirmOrderPayment(anyString(), anyString(), anyLong()))
//                .willReturn(paymentResponse);
//        willThrow(RuntimeException.class)
//                .given(orderDomainService).completedOrder(any());
//        given(orderDomainService.canceledOrder(anyString(), any()))
//                .willReturn(canceledOrder);
//        //when
//        //then
//        assertThatThrownBy(() -> orderFacade.finalizeOrder(ORDER_NO, USER_ID, paymentKey, FIXED_FINAL_PRICE))
//                .isInstanceOf(BusinessException.class)
//                .extracting("errorCode")
//                .isEqualTo(CommonErrorCode.INTERNAL_ERROR);
//
//        verify(orderDomainService).canceledOrder(ORDER_NO, OrderFailureCode.SYSTEM_ERROR);
//        verify(eventPublisher, times(1)).publishEvent(paymentResultEventCaptor.capture());
//        assertThat(paymentResultEventCaptor.getValue())
//                .extracting(PaymentResultEvent::getOrderNo, PaymentResultEvent::getStatus, PaymentResultEvent::getCode)
//                .containsExactly(ORDER_NO, OrderEventStatus.FAILURE, OrderFailureCode.SYSTEM_ERROR);
//    }

//    @Test
//    @DisplayName("주문을 조회한다")
//    void getOrder(){
//        //given
//        OrderDto orderDto = mockSavedOrder(OrderStatus.COMPLETED, FIXED_FINAL_PRICE);
//        given(orderService.getOrder(anyString(), anyLong()))
//                .willReturn(orderDto);
//        //when
//        OrderDetailResponse result = orderFacade.getOrder(USER_ID, ORDER_NO);
//        //then
//        assertThat(result)
//                .extracting(OrderDetailResponse::getOrderNo, OrderDetailResponse::getUserId, OrderDetailResponse::getOrderStatus, OrderDetailResponse::getOrderName,
//                        OrderDetailResponse::getDeliveryAddress)
//                .containsExactly(ORDER_NO, USER_ID, "COMPLETED", "상품1 외 1건", ADDRESS);
//
//        assertThat(result.getOrderPriceResponse())
//                .extracting(
//                        OrderDetailResponse.OrderPriceResponse::getTotalOriginPrice,
//                        OrderDetailResponse.OrderPriceResponse::getTotalProductDiscount,
//                        OrderDetailResponse.OrderPriceResponse::getCouponDiscount,
//                        OrderDetailResponse.OrderPriceResponse::getPointDiscount,
//                        OrderDetailResponse.OrderPriceResponse::getFinalPaymentAmount
//                )
//                        .containsExactly(
//                                34000L,
//                                3400L,
//                                COUPON_DISCOUNT,
//                                USE_POINT,
//                                FIXED_FINAL_PRICE
//                        );
//
//        assertThat(result.getCouponResponse())
//                .extracting(OrderDetailResponse.CouponResponse::getCouponId,
//                        OrderDetailResponse.CouponResponse::getCouponName,
//                        OrderDetailResponse.CouponResponse::getCouponDiscount)
//                .containsExactly(1L, "1000원 할인 쿠폰", COUPON_DISCOUNT);
//
//        assertThat(result.getOrderItems())
//                .hasSize(2)
//                .extracting(
//                        OrderItemResponse::getProductId,
//                        OrderItemResponse::getProductName,
//                        OrderItemResponse::getQuantity,
//                        OrderItemResponse::getLineTotal
//                )
//                .containsExactly(
//                        tuple(PROD_1_ID, "상품1", 3, 8100L),
//                        tuple(PROD_2_ID, "상품2", 5, 22500L)
//                );
//    }

//    @Test
//    @DisplayName("주문 목록을 조회한다")
//    void getOrders() {
//        //given
//        OrderSearchCondition condition = OrderSearchCondition.builder()
//                .page(1)
//                .size(10)
//                .sort("latest")
//                .build();
//        OrderDto orderDto = mockSavedOrder(OrderStatus.COMPLETED, FIXED_FINAL_PRICE);
//
//        Page<OrderDto> pageOrderDto = new PageImpl<>(
//                List.of(orderDto, orderDto),
//                PageRequest.of(0, 10),
//                100
//        );
//        given(orderService.getOrders(anyLong(), any(OrderSearchCondition.class)))
//                .willReturn(pageOrderDto);
//        //when
//        PageDto<OrderListResponse> result = orderFacade.getOrders(USER_ID, condition);
//        //then
//        assertThat(result)
//                .extracting(
//                        PageDto::getCurrentPage,
//                        PageDto::getTotalPage,
//                        PageDto::getPageSize,
//                        PageDto::getTotalElement
//                )
//                .containsExactly(1, 10L, 10, 100L);
//
//        assertThat(result.getContent())
//                .hasSize(2)
//                .extracting(
//                        OrderListResponse::getOrderNo,
//                        OrderListResponse::getOrderStatus,
//                        OrderListResponse::getUserId
//                )
//                .containsExactly(
//                        tuple(ORDER_NO, "COMPLETED", USER_ID),
//                        tuple(ORDER_NO, "COMPLETED", USER_ID)
//                );
//
//        assertThat(result.getContent().get(0).getOrderItems())
//                .hasSize(2)
//                .extracting("productName")
//                .contains("상품1", "상품2");
//    }
}

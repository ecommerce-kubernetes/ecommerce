package com.example.order_service.api.order.facade;

import com.example.order_service.api.common.dto.PageDto;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CommonErrorCode;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.common.exception.PaymentErrorCode;
import com.example.order_service.api.order.facade.dto.command.CreateOrderDto;
import com.example.order_service.api.order.facade.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.facade.dto.result.OrderDetailResponse;
import com.example.order_service.api.order.facade.dto.result.OrderItemResponse;
import com.example.order_service.api.order.facade.dto.result.OrderListResponse;
import com.example.order_service.api.order.facade.event.OrderCreatedEvent;
import com.example.order_service.api.order.facade.event.OrderEventStatus;
import com.example.order_service.api.order.facade.event.OrderResultEvent;
import com.example.order_service.api.order.facade.event.PaymentResultEvent;
import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.service.OrderDomainService;
import com.example.order_service.api.order.domain.service.OrderPriceCalculator;
import com.example.order_service.api.order.domain.service.dto.command.CreateOrderCommand;
import com.example.order_service.api.order.domain.service.dto.command.PaymentCreationCommand;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.infrastructure.OrderExternalAdaptor;
import com.example.order_service.api.order.infrastructure.client.payment.dto.response.TossPaymentConfirmResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static com.example.order_service.api.support.fixture.OrderApplicationServiceTestFixture.*;
import static org.assertj.core.api.Assertions.*;
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
    private OrderExternalAdaptor orderExternalAdaptor;
    @Mock
    private OrderDomainService orderDomainService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Spy
    private OrderDtoMapper mapper;
    @Spy
    private OrderPriceCalculator calculator;

    @Captor
    private ArgumentCaptor<OrderCreatedEvent> orderCreatedEventCaptor;
    @Captor
    private ArgumentCaptor<OrderResultEvent> orderResultEventCaptor;
    @Captor
    private ArgumentCaptor<PaymentResultEvent> paymentResultEventCaptor;

    @Test
    @DisplayName("주문을 생성한다")
    void initialOrder(){
        //given
        CreateOrderDto orderRequest = createOrderRequest(USER_ID,
                createRequestItem(1L, 3), createRequestItem(2L, 5));

        Long expectedAmount = 28600L;
        String expectedOrderName = "상품1 외 1건";

        given(orderExternalAdaptor.getOrderUser(anyLong())).willReturn(mockUserResponse());
        given(orderExternalAdaptor.getOrderProducts(anyList())).willReturn(List.of(
                mockProductResponse(1L, 3000L),
                mockProductResponse(2L, 5000L)
        ));
        given(orderExternalAdaptor.getCoupon(anyLong(), anyLong(), anyLong())).willReturn(mockCouponResponse());
        given(orderDomainService.saveOrder(any(CreateOrderCommand.class)))
                .willReturn(mockSavedOrder(OrderStatus.PENDING, expectedAmount));
        //when
        CreateOrderResponse response = orderFacade.initialOrder(orderRequest);
        //then
        assertThat(response.getOrderNo()).isNotNull();
        assertThat(response)
                .extracting(CreateOrderResponse::getStatus, CreateOrderResponse::getOrderName, CreateOrderResponse::getFinalPaymentAmount)
                .contains("PENDING", expectedOrderName, expectedAmount);
        assertThat(response.getCreatedAt()).isNotNull();

        verify(eventPublisher, times(1)).publishEvent(orderCreatedEventCaptor.capture());
        OrderCreatedEvent event = orderCreatedEventCaptor.getValue();

        assertThat(event)
                .extracting(OrderCreatedEvent::getOrderNo, OrderCreatedEvent::getUserId, OrderCreatedEvent::getCouponId, OrderCreatedEvent::getUsedPoint)
                .containsExactly(ORDER_NO, 1L, 1L, 1000L);

        assertThat(event.getOrderedItems())
                .hasSize(2)
                .extracting("productVariantId", "quantity")
                .containsExactlyInAnyOrder(
                        tuple(1L, 3),
                        tuple(2L, 5)
                );
    }

    @Test
    @DisplayName("주문의 상태를 결제 대기로 변경한다")
    void preparePayment() {
        //given
        Long expectedAmount = 28600L;
        OrderDto orderDto = mockSavedOrder(OrderStatus.PAYMENT_WAITING, expectedAmount);
        given(orderDomainService.changeOrderStatus(ORDER_NO, OrderStatus.PAYMENT_WAITING))
                .willReturn(orderDto);
        //when
        orderFacade.preparePayment(ORDER_NO);
        //then
        verify(orderDomainService, times(1)).changeOrderStatus(ORDER_NO, OrderStatus.PAYMENT_WAITING);
        verify(eventPublisher, times(1)).publishEvent(orderResultEventCaptor.capture());

        assertThat(orderResultEventCaptor.getValue())
                .extracting(OrderResultEvent::getOrderNo, OrderResultEvent::getUserId, OrderResultEvent::getStatus,
                        OrderResultEvent::getCode, OrderResultEvent::getOrderName, OrderResultEvent::getFinalPaymentAmount)
                .containsExactly(ORDER_NO, USER_ID, OrderEventStatus.SUCCESS, "PAYMENT_READY",
                        "상품1 외 1건", expectedAmount);
    }

    @Test
    @DisplayName("주문의 상태 취소로 변경, 실패 코드 추가 후 주문 결과 이벤트 발행")
    void processOrderFailure() {
        //given
        OrderFailureCode failureCode = OrderFailureCode.OUT_OF_STOCK;
        OrderDto canceledOrder = mockCanceledOrder(failureCode);
        given(orderDomainService.canceledOrder(ORDER_NO, failureCode))
                .willReturn(canceledOrder);
        //when
        orderFacade.processOrderFailure(ORDER_NO, failureCode);
        //then
        verify(orderDomainService, times(1)).canceledOrder(ORDER_NO, failureCode);

        verify(eventPublisher, times(1))
                .publishEvent(orderResultEventCaptor.capture());

        assertThat(orderResultEventCaptor.getValue())
                .extracting(OrderResultEvent::getOrderNo, OrderResultEvent::getUserId, OrderResultEvent::getStatus,
                        OrderResultEvent::getCode, OrderResultEvent::getOrderName, OrderResultEvent::getFinalPaymentAmount)
                .containsExactly(ORDER_NO, USER_ID, OrderEventStatus.FAILURE, "OUT_OF_STOCK",
                        "상품1 외 1건", 28600L);
    }

    @Test
    @DisplayName("결제가 승인되면 주문상태를 성공으로 변경, 결제 승인 이벤트를 발행하고 응답을 반환한다")
    void finalizeOrder(){
        //given
        String paymentKey = "paymentKey";
        Long amount = 28600L;
        OrderDto waitingOrder = mockSavedOrder(OrderStatus.PAYMENT_WAITING, amount);
        TossPaymentConfirmResponse paymentResponse = mockPaymentResponse(paymentKey, amount);
        OrderDto completedOrder = mockSavedOrder(OrderStatus.COMPLETED, amount);
        given(orderDomainService.getOrder(ORDER_NO, USER_ID))
                .willReturn(waitingOrder);
        given(orderExternalAdaptor.confirmOrderPayment(anyString(), anyString(), anyLong()))
                .willReturn(paymentResponse);
        given(orderDomainService.completedOrder(any(PaymentCreationCommand.class)))
                .willReturn(completedOrder);
        //when
        OrderDetailResponse result = orderFacade.finalizeOrder(ORDER_NO, USER_ID, paymentKey, amount);
        //then
        assertThat(result)
                .extracting(OrderDetailResponse::getOrderNo, OrderDetailResponse::getUserId, OrderDetailResponse::getOrderStatus, OrderDetailResponse::getOrderName,
                        OrderDetailResponse::getDeliveryAddress)
                .containsExactly(ORDER_NO, USER_ID, "COMPLETED", "상품1 외 1건", ADDRESS);
        assertThat(result.getOrderItems()).isNotEmpty();

        verify(eventPublisher, times(1)).publishEvent(paymentResultEventCaptor.capture());

        assertThat(paymentResultEventCaptor.getValue())
                .extracting(PaymentResultEvent::getOrderNo, PaymentResultEvent::getStatus, PaymentResultEvent::getCode)
                .containsExactly(ORDER_NO, OrderEventStatus.SUCCESS, null);
    }

    @Test
    @DisplayName("결제를 승인할때 주문 상태가 결제 대기 상태가 아니면 예외를 던진다")
    void finalizeOrder_with_notPaymentWaiting(){
        //given
        OrderDto invalidStatusOrder = mockSavedOrder(OrderStatus.PENDING, FIXED_FINAL_PRICE);
        given(orderDomainService.getOrder(anyString(), anyLong()))
                .willReturn(invalidStatusOrder);
        //when
        //then
        assertThatThrownBy(() -> orderFacade.finalizeOrder(ORDER_NO, USER_ID, "paymentKey", FIXED_FINAL_PRICE))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_NOT_PAYABLE);
    }

    @Test
    @DisplayName("결제를 승인할때 요청의 amount 와 실제 최종 주문 금액이 다르면 예외를 던진다")
    void finalizeOrder_with_missMatch_Price(){
        //given
        Long requestedAmount = 30000L;
        OrderDto orderDto = mockSavedOrder(OrderStatus.PAYMENT_WAITING, FIXED_FINAL_PRICE);
        given(orderDomainService.getOrder(anyString(), anyLong()))
                .willReturn(orderDto);
        //when
        //then
        assertThatThrownBy(() -> orderFacade.finalizeOrder(ORDER_NO, USER_ID, "paymentKey", requestedAmount))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_PRICE_MISMATCH);
    }

    @Test
    @DisplayName("결제를 승인할때 결제 승인이 실패한 경우 주문을 실패 처리, 주문 실패 이벤트를 발행하고 예외를 그대로 던진다")
    void finalizeOrder_when_payment_fail(){
        //given
        String paymentKey = "paymentKey";
        String failureMessage = "결제 승인이 거절되었습니다";

        OrderDto waitingOrder = mockSavedOrder(OrderStatus.PAYMENT_WAITING, FIXED_FINAL_PRICE);
        OrderDto failureOrder = mockCanceledOrder(OrderFailureCode.PAYMENT_FAILED);
        given(orderDomainService.getOrder(anyString(), anyLong()))
                .willReturn(waitingOrder);
        willThrow(new BusinessException(PaymentErrorCode.PAYMENT_APPROVAL_FAIL))
                .given(orderExternalAdaptor).confirmOrderPayment(anyString(), anyString(), anyLong());
        given(orderDomainService.canceledOrder(anyString(), any(OrderFailureCode.class)))
                .willReturn(failureOrder);
        //when
        //then
        assertThatThrownBy(() -> orderFacade.finalizeOrder(ORDER_NO, USER_ID, paymentKey, FIXED_FINAL_PRICE))
                .isInstanceOf(BusinessException.class)
                .hasMessage(failureMessage);

        verify(orderDomainService, times(1)).canceledOrder(ORDER_NO, OrderFailureCode.PAYMENT_FAILED);

        verify(eventPublisher, times(1)).publishEvent(paymentResultEventCaptor.capture());
        assertThat(paymentResultEventCaptor.getValue())
                .extracting(PaymentResultEvent::getOrderNo, PaymentResultEvent::getStatus, PaymentResultEvent::getCode)
                .containsExactly(ORDER_NO, OrderEventStatus.FAILURE, OrderFailureCode.PAYMENT_FAILED);
    }

    @Test
    @DisplayName("결제 승인 요청시 결제는 승인되었지만 주문 상태 변경이 실패한 경우 주문을 실패 처리, SAGA 보상을 진행하고 예외를 던진다")
    void finalizeOrder_when_DB_Exception(){
        //given
        String paymentKey = "paymentKey";
        OrderDto waitingOrder = mockSavedOrder(OrderStatus.PAYMENT_WAITING, FIXED_FINAL_PRICE);
        OrderDto canceledOrder = mockSavedOrder(OrderStatus.CANCELED, FIXED_FINAL_PRICE);
        given(orderDomainService.getOrder(anyString(), anyLong()))
                .willReturn(waitingOrder);
        TossPaymentConfirmResponse paymentResponse = mockPaymentResponse(paymentKey, FIXED_FINAL_PRICE);
        given(orderExternalAdaptor.confirmOrderPayment(anyString(), anyString(), anyLong()))
                .willReturn(paymentResponse);
        willThrow(RuntimeException.class)
                .given(orderDomainService).completedOrder(any());
        given(orderDomainService.canceledOrder(anyString(), any()))
                .willReturn(canceledOrder);
        //when
        //then
        assertThatThrownBy(() -> orderFacade.finalizeOrder(ORDER_NO, USER_ID, paymentKey, FIXED_FINAL_PRICE))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CommonErrorCode.INTERNAL_ERROR);

        verify(orderDomainService).canceledOrder(ORDER_NO, OrderFailureCode.SYSTEM_ERROR);
        verify(eventPublisher, times(1)).publishEvent(paymentResultEventCaptor.capture());
        assertThat(paymentResultEventCaptor.getValue())
                .extracting(PaymentResultEvent::getOrderNo, PaymentResultEvent::getStatus, PaymentResultEvent::getCode)
                .containsExactly(ORDER_NO, OrderEventStatus.FAILURE, OrderFailureCode.SYSTEM_ERROR);
    }

    @Test
    @DisplayName("주문을 조회한다")
    void getOrder(){
        //given
        OrderDto orderDto = mockSavedOrder(OrderStatus.COMPLETED, FIXED_FINAL_PRICE);
        given(orderDomainService.getOrder(anyString(), anyLong()))
                .willReturn(orderDto);
        //when
        OrderDetailResponse result = orderFacade.getOrder(USER_ID, ORDER_NO);
        //then
        assertThat(result)
                .extracting(OrderDetailResponse::getOrderNo, OrderDetailResponse::getUserId, OrderDetailResponse::getOrderStatus, OrderDetailResponse::getOrderName,
                        OrderDetailResponse::getDeliveryAddress)
                .containsExactly(ORDER_NO, USER_ID, "COMPLETED", "상품1 외 1건", ADDRESS);

        assertThat(result.getOrderPriceResponse())
                .extracting(
                        OrderDetailResponse.OrderPriceResponse::getTotalOriginPrice,
                        OrderDetailResponse.OrderPriceResponse::getTotalProductDiscount,
                        OrderDetailResponse.OrderPriceResponse::getCouponDiscount,
                        OrderDetailResponse.OrderPriceResponse::getPointDiscount,
                        OrderDetailResponse.OrderPriceResponse::getFinalPaymentAmount
                )
                        .containsExactly(
                                34000L,
                                3400L,
                                COUPON_DISCOUNT,
                                USE_POINT,
                                FIXED_FINAL_PRICE
                        );

        assertThat(result.getCouponResponse())
                .extracting(OrderDetailResponse.CouponResponse::getCouponId,
                        OrderDetailResponse.CouponResponse::getCouponName,
                        OrderDetailResponse.CouponResponse::getCouponDiscount)
                .containsExactly(1L, "1000원 할인 쿠폰", COUPON_DISCOUNT);

        assertThat(result.getOrderItems())
                .hasSize(2)
                .extracting(
                        OrderItemResponse::getProductId,
                        OrderItemResponse::getProductName,
                        OrderItemResponse::getQuantity,
                        OrderItemResponse::getLineTotal
                )
                .containsExactly(
                        tuple(PROD_1_ID, "상품1", 3, 8100L),
                        tuple(PROD_2_ID, "상품2", 5, 22500L)
                );
    }

    @Test
    @DisplayName("주문 목록을 조회한다")
    void getOrders() {
        //given
        OrderSearchCondition condition = OrderSearchCondition.builder()
                .page(1)
                .size(10)
                .sort("latest")
                .build();
        OrderDto orderDto = mockSavedOrder(OrderStatus.COMPLETED, FIXED_FINAL_PRICE);

        Page<OrderDto> pageOrderDto = new PageImpl<>(
                List.of(orderDto, orderDto),
                PageRequest.of(0, 10),
                100
        );
        given(orderDomainService.getOrders(anyLong(), any(OrderSearchCondition.class)))
                .willReturn(pageOrderDto);
        //when
        PageDto<OrderListResponse> result = orderFacade.getOrders(USER_ID, condition);
        //then
        assertThat(result)
                .extracting(
                        PageDto::getCurrentPage,
                        PageDto::getTotalPage,
                        PageDto::getPageSize,
                        PageDto::getTotalElement
                )
                .containsExactly(1, 10L, 10, 100L);

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(
                        OrderListResponse::getOrderNo,
                        OrderListResponse::getOrderStatus,
                        OrderListResponse::getUserId
                )
                .containsExactly(
                        tuple(ORDER_NO, "COMPLETED", USER_ID),
                        tuple(ORDER_NO, "COMPLETED", USER_ID)
                );

        assertThat(result.getContent().get(0).getOrderItems())
                .hasSize(2)
                .extracting("productName")
                .contains("상품1", "상품2");
    }
}

package com.example.order_service.api.order.application;

import com.example.order_service.api.common.dto.PageDto;
import com.example.order_service.api.common.exception.NoPermissionException;
import com.example.order_service.api.common.exception.OrderVerificationException;
import com.example.order_service.api.common.exception.PaymentErrorCode;
import com.example.order_service.api.common.exception.PaymentException;
import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.application.dto.result.OrderDetailResponse;
import com.example.order_service.api.order.application.dto.result.OrderItemResponse;
import com.example.order_service.api.order.application.dto.result.OrderListResponse;
import com.example.order_service.api.order.application.event.*;
import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.service.OrderDomainService;
import com.example.order_service.api.order.domain.service.OrderPriceCalculator;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.PaymentCreationCommand;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.infrastructure.OrderIntegrationService;
import com.example.order_service.api.order.infrastructure.client.payment.dto.TossPaymentConfirmResponse;
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
public class OrderApplicationServiceTest {

    @InjectMocks
    private OrderApplicationService orderApplicationService;
    @Mock
    private OrderIntegrationService orderIntegrationService;
    @Mock
    private OrderDomainService orderDomainService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
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
    void createOrder(){
        //given
        CreateOrderDto orderRequest = createOrderRequest(USER_ID,
                createRequestItem(1L, 3), createRequestItem(2L, 5));

        Long expectedAmount = 28600L;
        String expectedOrderName = "상품1 외 1건";

        given(orderIntegrationService.getOrderUser(any())).willReturn(mockUserResponse());
        given(orderIntegrationService.getOrderProducts(anyList())).willReturn(List.of(
                mockProductResponse(1L, 3000L),
                mockProductResponse(2L, 5000L)
        ));
        given(orderIntegrationService.getCoupon(any(), anyLong(), anyLong())).willReturn(mockCouponResponse());
        given(orderDomainService.saveOrder(any(OrderCreationContext.class)))
                .willReturn(mockSavedOrder(OrderStatus.PENDING, expectedAmount));
        //when
        CreateOrderResponse response = orderApplicationService.createOrder(orderRequest);
        //then
        assertThat(response.getOrderId()).isNotNull();
        assertThat(response)
                .extracting(CreateOrderResponse::getStatus, CreateOrderResponse::getOrderName, CreateOrderResponse::getFinalPaymentAmount)
                .contains("PENDING", expectedOrderName, expectedAmount);
        assertThat(response.getCreatedAt()).isNotNull();

        verify(eventPublisher, times(1)).publishEvent(orderCreatedEventCaptor.capture());
        OrderCreatedEvent event = orderCreatedEventCaptor.getValue();

        assertThat(event)
                .extracting(OrderCreatedEvent::getOrderId, OrderCreatedEvent::getUserId, OrderCreatedEvent::getCouponId, OrderCreatedEvent::getUsedPoint)
                .containsExactly(1L, 1L, 1L, 1000L);

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
    void changePaymentWaiting() {
        //given
        Long orderId = 1L;
        Long expectedAmount = 28600L;
        OrderDto orderDto = mockSavedOrder(OrderStatus.PAYMENT_WAITING, expectedAmount);
        given(orderDomainService.changeOrderStatus(orderId, OrderStatus.PAYMENT_WAITING))
                .willReturn(orderDto);
        //when
        orderApplicationService.changePaymentWaiting(orderId);
        //then
        verify(orderDomainService, times(1)).changeOrderStatus(orderId, OrderStatus.PAYMENT_WAITING);
        verify(eventPublisher, times(1)).publishEvent(orderResultEventCaptor.capture());

        assertThat(orderResultEventCaptor.getValue())
                .extracting(OrderResultEvent::getOrderId, OrderResultEvent::getUserId, OrderResultEvent::getStatus,
                        OrderResultEvent::getCode, OrderResultEvent::getOrderName, OrderResultEvent::getFinalPaymentAmount)
                .containsExactly(orderId, USER_ID, OrderEventStatus.SUCCESS, OrderEventCode.PAYMENT_READY,
                        "상품1 외 1건", expectedAmount);
    }

    @Test
    @DisplayName("주문의 상태 취소로 변경, 실패 코드 추가 후 주문 결과 이벤트 발행")
    void changeCanceled() {
        //given
        Long orderId = 1L;
        OrderFailureCode failureCode = OrderFailureCode.OUT_OF_STOCK;
        OrderDto canceledOrder = mockCanceledOrder(failureCode);
        given(orderDomainService.canceledOrder(orderId, failureCode))
                .willReturn(canceledOrder);
        //when
        orderApplicationService.changeCanceled(orderId, failureCode);
        //then
        verify(orderDomainService, times(1)).canceledOrder(orderId, failureCode);

        verify(eventPublisher, times(1))
                .publishEvent(orderResultEventCaptor.capture());

        assertThat(orderResultEventCaptor.getValue())
                .extracting(OrderResultEvent::getOrderId, OrderResultEvent::getUserId, OrderResultEvent::getStatus,
                        OrderResultEvent::getCode, OrderResultEvent::getOrderName, OrderResultEvent::getFinalPaymentAmount)
                .containsExactly(orderId, USER_ID, OrderEventStatus.FAILURE, OrderEventCode.OUT_OF_STOCK,
                        "상품1 외 1건", null);
    }

    @Test
    @DisplayName("결제가 승인되면 주문상태를 성공으로 변경, 결제 승인 이벤트를 발행하고 응답을 반환한다")
    void confirmOrder(){
        //given
        Long orderId = 1L;
        String paymentKey = "paymentKey";
        Long amount = 28600L;
        OrderDto waitingOrder = mockSavedOrder(OrderStatus.PAYMENT_WAITING, amount);
        TossPaymentConfirmResponse paymentResponse = mockPaymentResponse(paymentKey, amount);
        OrderDto completedOrder = mockSavedOrder(OrderStatus.COMPLETED, amount);
        given(orderDomainService.getOrder(orderId))
                .willReturn(waitingOrder);
        given(orderIntegrationService.confirmOrderPayment(anyLong(), anyString(), anyLong()))
                .willReturn(paymentResponse);
        given(orderDomainService.completedOrder(any(PaymentCreationCommand.class)))
                .willReturn(completedOrder);
        //when
        OrderDetailResponse result = orderApplicationService.confirmOrder(orderId, paymentKey);
        //then
        assertThat(result)
                .extracting(OrderDetailResponse::getOrderId, OrderDetailResponse::getUserId, OrderDetailResponse::getOrderStatus, OrderDetailResponse::getOrderName,
                        OrderDetailResponse::getDeliveryAddress)
                .containsExactly(orderId, USER_ID, "COMPLETED", "상품1 외 1건", ADDRESS);
        assertThat(result.getOrderItems()).isNotEmpty();

        verify(eventPublisher, times(1)).publishEvent(paymentResultEventCaptor.capture());

        assertThat(paymentResultEventCaptor.getValue())
                .extracting(PaymentResultEvent::getOrderId, PaymentResultEvent::getStatus, PaymentResultEvent::getCode)
                .containsExactly(orderId, OrderEventStatus.SUCCESS, OrderEventCode.PAYMENT_AUTHORIZED);
        assertThat(paymentResultEventCaptor.getValue().getFailureReason()).isNull();
    }

    @Test
    @DisplayName("결제를 승인할때 주문 상태가 결제 대기 상태가 아니면 예외를 던진다")
    void confirmOrder_with_notPaymentWaiting(){
        //given
        Long orderId = 1L;
        OrderDto invalidStatusOrder = mockSavedOrder(OrderStatus.PENDING, FIXED_FINAL_PRICE);
        given(orderDomainService.getOrder(anyLong()))
                .willReturn(invalidStatusOrder);
        //when
        //then
        assertThatThrownBy(() -> orderApplicationService.confirmOrder(orderId, "paymentKey"))
                .isInstanceOf(OrderVerificationException.class)
                .hasMessage("결제 가능한 주문이 아닙니다");
    }

    @Test
    @DisplayName("결제를 승인할때 결제 승인이 실패한 경우 주문을 실패 처리, 주문 실패 이벤트를 발행하고 예외를 그대로 던진다")
    void confirmOrder_when_payment_fail(){
        //given
        Long orderId = 1L;
        String paymentKey = "paymentKey";
        String failureMessage = "결제 승인이 실패했습니다";

        OrderDto waitingOrder = mockSavedOrder(OrderStatus.PAYMENT_WAITING, FIXED_FINAL_PRICE);
        OrderDto failureOrder = mockCanceledOrder(OrderFailureCode.PAYMENT_FAILED);
        given(orderDomainService.getOrder(anyLong()))
                .willReturn(waitingOrder);
        willThrow(new PaymentException(failureMessage, PaymentErrorCode.APPROVAL_FAIL))
                .given(orderIntegrationService).confirmOrderPayment(anyLong(), anyString(), anyLong());
        given(orderDomainService.canceledOrder(anyLong(), any(OrderFailureCode.class)))
                .willReturn(failureOrder);
        //when
        //then
        assertThatThrownBy(() -> orderApplicationService.confirmOrder(orderId, paymentKey))
                .isInstanceOf(PaymentException.class)
                .hasMessage(failureMessage);

        verify(orderDomainService, times(1)).canceledOrder(orderId, OrderFailureCode.PAYMENT_FAILED);

        verify(eventPublisher, times(1)).publishEvent(paymentResultEventCaptor.capture());
        assertThat(paymentResultEventCaptor.getValue())
                .extracting(PaymentResultEvent::getOrderId, PaymentResultEvent::getStatus, PaymentResultEvent::getCode, PaymentResultEvent::getFailureReason)
                .containsExactly(orderId, OrderEventStatus.FAILURE, OrderEventCode.PAYMENT_AUTHORIZED_FAILED, failureMessage);
    }

    @Test
    @DisplayName("주문을 조회한다")
    void getOrder(){
        //given
        Long orderId = 1L;
        UserPrincipal userPrincipal = UserPrincipal.of(USER_ID, UserRole.ROLE_USER);
        OrderDto orderDto = mockSavedOrder(OrderStatus.COMPLETED, FIXED_FINAL_PRICE);
        given(orderDomainService.getOrder(orderId))
                .willReturn(orderDto);
        //when
        OrderDetailResponse result = orderApplicationService.getOrder(userPrincipal, orderId);
        //then
        assertThat(result)
                .extracting(OrderDetailResponse::getOrderId, OrderDetailResponse::getUserId, OrderDetailResponse::getOrderStatus, OrderDetailResponse::getOrderName,
                        OrderDetailResponse::getDeliveryAddress)
                .containsExactly(orderId, USER_ID, "COMPLETED", "상품1 외 1건", ADDRESS);

        assertThat(result.getPaymentResponse())
                .extracting(
                        OrderDetailResponse.PaymentResponse::getTotalOriginPrice,
                        OrderDetailResponse.PaymentResponse::getTotalProductDiscount,
                        OrderDetailResponse.PaymentResponse::getCouponDiscount,
                        OrderDetailResponse.PaymentResponse::getPointDiscount,
                        OrderDetailResponse.PaymentResponse::getFinalPaymentAmount
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
    @DisplayName("주문의 유저 아이디와 요청한 유저아이디가 일치하지 않는 경우 예외를 던진다")
    void getOrder_not_match_userId(){
        //given
        Long orderId = 1L;
        Long otherUserId = 999L;
        OrderDto orderDto = mockSavedOrder(OrderStatus.COMPLETED, FIXED_FINAL_PRICE);
        UserPrincipal stranger = UserPrincipal.of(otherUserId, UserRole.ROLE_USER);

        given(orderDomainService.getOrder(orderId))
                .willReturn(orderDto);
        //when
        //then
        assertThatThrownBy(() -> orderApplicationService.getOrder(stranger, orderId))
                .isInstanceOf(NoPermissionException.class)
                .hasMessage("주문을 조회할 권한이 없습니다");
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
        PageDto<OrderListResponse> result = orderApplicationService.getOrders(UserPrincipal.of(USER_ID, UserRole.ROLE_USER), condition);
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
                        OrderListResponse::getOrderId,
                        OrderListResponse::getOrderStatus,
                        OrderListResponse::getUserId
                )
                .containsExactly(
                        tuple(1L, "COMPLETED", USER_ID),
                        tuple(1L, "COMPLETED", USER_ID)
                );

        assertThat(result.getContent().get(0).getOrderItems())
                .hasSize(2)
                .extracting("productName")
                .contains("상품1", "상품2");
    }
}

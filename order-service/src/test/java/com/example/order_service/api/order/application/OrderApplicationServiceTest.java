package com.example.order_service.api.order.application;

import com.example.order_service.api.common.exception.NoPermissionException;
import com.example.order_service.api.common.exception.OrderVerificationException;
import com.example.order_service.api.common.exception.PaymentErrorCode;
import com.example.order_service.api.common.exception.PaymentException;
import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.application.dto.result.OrderItemResponse;
import com.example.order_service.api.order.application.dto.result.OrderDetailResponse;
import com.example.order_service.api.order.application.event.*;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.model.vo.AppliedCoupon;
import com.example.order_service.api.order.domain.model.vo.PaymentInfo;
import com.example.order_service.api.order.domain.service.OrderDomainService;
import com.example.order_service.api.order.domain.service.OrderPriceCalculator;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import com.example.order_service.api.order.infrastructure.OrderIntegrationService;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import com.example.order_service.api.order.infrastructure.client.payment.dto.TossPaymentConfirmResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;

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

    @Test
    @DisplayName("주문을 생성한다")
    void createOrder(){
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        CreateOrderItemDto orderItem1 = createOrderItemDto(1L, 3);
        CreateOrderItemDto orderItem2 = createOrderItemDto(2L, 5);

        CreateOrderDto createOrderDto = createOrderDto(userPrincipal, "서울시 테헤란로 123", 1L, 1000L,
                28600L, orderItem1, orderItem2);

        OrderUserResponse user = OrderUserResponse.builder()
                .userId(1L).pointBalance(3000L).build();
        OrderProductResponse product1 = createProductResponse(1L, 1L, "상품1", 3000L, 10,
                "http://thumbnail1.jpg", 100,
                List.of(OrderProductResponse.ItemOption.builder().optionTypeName("사이즈").optionValueName("XL").build()));
        OrderProductResponse product2 = createProductResponse(2L, 2L, "상품2", 5000L, 10,
                "http://thumbnail2.jpg", 100,
                List.of(OrderProductResponse.ItemOption.builder().optionTypeName("용량").optionValueName("256GB").build()));
        OrderCouponCalcResponse coupon = OrderCouponCalcResponse.builder()
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L)
                .build();


        OrderItemDto savedProduct1 = createOrderItemDto(1L, 1L, "상품1", "http://thumbnail1.jpg", 3, 3000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("사이즈").optionValueName("XL").build()));
        OrderItemDto savedProduct2 = createOrderItemDto(2L, 2L, "상품2", "http://thumbnail1.jpg", 5, 5000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("용량").optionValueName("256GB").build()));
        AppliedCoupon appliedCoupon = createAppliedCoupon(1L, "1000원 할인 쿠폰", 1000L);
        PaymentInfo paymentInfo = createPaymentInfo(34000, 3400, 1000, 1000, 28600);
        OrderDto orderDto = createOrderDto(1L, OrderStatus.PENDING, "상품1 외 1건", "서울시 테헤란로 123", paymentInfo, List.of(savedProduct1, savedProduct2), appliedCoupon,
                null);

        given(orderIntegrationService.getOrderUser(any(UserPrincipal.class))).willReturn(user);
        given(orderIntegrationService.getOrderProducts(anyList()))
                .willReturn(List.of(product1, product2));
        given(orderIntegrationService.getCoupon(any(UserPrincipal.class), anyLong(), anyLong()))
                .willReturn(coupon);
        given(orderDomainService.saveOrder(any(OrderCreationContext.class)))
                .willReturn(orderDto);
        //when
        CreateOrderResponse response = orderApplicationService.createOrder(createOrderDto);
        //then
        assertThat(response.getOrderId()).isNotNull();
        assertThat(response)
                .extracting(CreateOrderResponse::getStatus, CreateOrderResponse::getOrderName, CreateOrderResponse::getFinalPaymentAmount)
                .contains("PENDING", "상품1 외 1건", 28600L);
        assertThat(response.getCreateAt()).isNotNull();

        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        OrderCreatedEvent publishedEvent = captor.getValue();
        assertThat(publishedEvent)
                .extracting(OrderCreatedEvent::getOrderId, OrderCreatedEvent::getUserId, OrderCreatedEvent::getCouponId, OrderCreatedEvent::getUsedPoint)
                .containsExactly(1L, 1L, 1L, 1000L);

        assertThat(publishedEvent.getOrderedItems())
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
        OrderItemDto savedProduct1 = createOrderItemDto(1L, 1L, "상품1", "http://thumbnail1.jpg", 3, 3000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("사이즈").optionValueName("XL").build()));
        OrderItemDto savedProduct2 = createOrderItemDto(2L, 2L, "상품2", "http://thumbnail1.jpg", 5, 5000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("용량").optionValueName("256GB").build()));
        AppliedCoupon appliedCoupon = createAppliedCoupon(1L, "1000원 할인 쿠폰", 1000L);
        PaymentInfo paymentInfo = createPaymentInfo(34000, 3400, 1000, 1000, 28600);
        OrderDto orderDto = createOrderDto(1L, OrderStatus.PAYMENT_WAITING, "상품1 외 1건", "서울시 테헤란로 123", paymentInfo, List.of(savedProduct1, savedProduct2), appliedCoupon,
                null);
        given(orderDomainService.changeOrderStatus(orderId, OrderStatus.PAYMENT_WAITING))
                .willReturn(orderDto);
        //when
        orderApplicationService.changePaymentWaiting(orderId);
        //then
        ArgumentCaptor<OrderResultEvent> orderResultCaptor = ArgumentCaptor.forClass(OrderResultEvent.class);
        verify(orderDomainService, times(1)).changeOrderStatus(orderId, OrderStatus.PAYMENT_WAITING);
        verify(eventPublisher, times(1)).publishEvent(orderResultCaptor.capture());

        assertThat(orderResultCaptor.getValue())
                .extracting(OrderResultEvent::getOrderId, OrderResultEvent::getUserId, OrderResultEvent::getStatus,
                        OrderResultEvent::getCode, OrderResultEvent::getOrderName, OrderResultEvent::getFinalPaymentAmount)
                .containsExactly(orderId, 1L, OrderEventStatus.SUCCESS, OrderEventCode.PAYMENT_READY,
                        "상품1 외 1건", 28600L);
    }

    @Test
    @DisplayName("주문의 상태를 주문 취소로 변경하고 실패 코드를 추가한다")
    void changeCanceled() {
        //given
        Long orderId = 1L;
        OrderFailureCode code = OrderFailureCode.OUT_OF_STOCK;
        OrderItemDto savedProduct1 = createOrderItemDto(1L, 1L, "상품1", "http://thumbnail1.jpg", 3, 3000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("사이즈").optionValueName("XL").build()));
        OrderItemDto savedProduct2 = createOrderItemDto(2L, 2L, "상품2", "http://thumbnail1.jpg", 5, 5000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("용량").optionValueName("256GB").build()));
        AppliedCoupon appliedCoupon = createAppliedCoupon(1L, "1000원 할인 쿠폰", 1000L);
        PaymentInfo paymentInfo = createPaymentInfo(34000, 3400, 1000, 1000, 28600);
        OrderDto orderDto = createOrderDto(1L, OrderStatus.CANCELED, "상품1 외 1건", "서울시 테헤란로 123", paymentInfo, List.of(savedProduct1, savedProduct2), appliedCoupon,
                code);
        given(orderDomainService.changeCanceled(orderId, code))
                .willReturn(orderDto);
        //when
        orderApplicationService.changeCanceled(orderId, code);
        //then
        ArgumentCaptor<OrderResultEvent> orderResultCaptor = ArgumentCaptor.forClass(OrderResultEvent.class);
        verify(orderDomainService, times(1)).changeCanceled(orderId, code);
        verify(eventPublisher, times(1))
                .publishEvent(orderResultCaptor.capture());

        assertThat(orderResultCaptor.getValue())
                .extracting(OrderResultEvent::getOrderId, OrderResultEvent::getUserId, OrderResultEvent::getStatus,
                        OrderResultEvent::getCode, OrderResultEvent::getOrderName, OrderResultEvent::getFinalPaymentAmount)
                .containsExactly(orderId, 1L, OrderEventStatus.FAILURE, OrderEventCode.OUT_OF_STOCK,
                        "상품1 외 1건", null);
    }

    @Test
    @DisplayName("결제가 승인되면 주문상태를 성공으로 변경, 결제 승인 이벤트를 발행하고 응답을 반환한다")
    void confirmOrder(){
        //given
        OrderItemDto savedProduct1 = createOrderItemDto(1L, 1L, "상품1", "http://thumbnail1.jpg", 3, 3000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("사이즈").optionValueName("XL").build()));
        OrderItemDto savedProduct2 = createOrderItemDto(2L, 2L, "상품2", "http://thumbnail1.jpg", 5, 5000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("용량").optionValueName("256GB").build()));
        AppliedCoupon appliedCoupon = createAppliedCoupon(1L, "1000원 할인 쿠폰", 1000L);
        PaymentInfo paymentInfo = createPaymentInfo(34000, 3400, 1000, 1000, 28600);
        OrderDto orderDto = createOrderDto(1L, OrderStatus.PAYMENT_WAITING, "상품1 외 1건", "서울시 테헤란로 123", paymentInfo, List.of(savedProduct1, savedProduct2), appliedCoupon,
                null);
        OrderDto completeOrderDto = createOrderDto(1L, OrderStatus.COMPLETED, "상품1 외 1건", "서울시 테헤란로 123", paymentInfo, List.of(savedProduct1, savedProduct2), appliedCoupon,
                null);
        TossPaymentConfirmResponse paymentConfirmResponse = TossPaymentConfirmResponse.builder()
                .paymentKey("paymentKey")
                .orderId(1L)
                .totalAmount(28600L)
                .status("DONE").build();

        given(orderDomainService.getOrder(anyLong()))
                .willReturn(orderDto);
        given(orderIntegrationService.confirmOrderPayment(anyLong(), anyString(), anyLong()))
                .willReturn(paymentConfirmResponse);
        given(orderDomainService.changeOrderStatus(anyLong(), any(OrderStatus.class)))
                .willReturn(completeOrderDto);
        //when
        OrderDetailResponse orderDetailResponse = orderApplicationService.confirmOrder(1L, "paymentKey");
        //then
        verify(orderDomainService, times(1)).changeOrderStatus(1L, OrderStatus.COMPLETED);
        assertThat(orderDetailResponse)
                .extracting(OrderDetailResponse::getOrderId, OrderDetailResponse::getUserId, OrderDetailResponse::getOrderStatus, OrderDetailResponse::getOrderName,
                        OrderDetailResponse::getDeliveryAddress)
                .containsExactly(1L, 1L, "COMPLETED", "상품1 외 1건", "서울시 테헤란로 123");

        assertThat(orderDetailResponse.getOrderItems()).isNotEmpty();

        ArgumentCaptor<PaymentResultEvent> paymentCaptor = ArgumentCaptor.forClass(PaymentResultEvent.class);
        verify(eventPublisher, times(1)).publishEvent(paymentCaptor.capture());

        assertThat(paymentCaptor.getValue())
                .extracting(PaymentResultEvent::getOrderId, PaymentResultEvent::getStatus, PaymentResultEvent::getCode)
                .containsExactly(1L, OrderEventStatus.SUCCESS, OrderEventCode.PAYMENT_AUTHORIZED);
        assertThat(paymentCaptor.getValue().getFailureReason()).isNull();
    }

    @Test
    @DisplayName("결제를 승인할때 주문 상태가 결제 대기 상태가 아니면 예외를 던진다")
    void confirmOrder_with_notPaymentWaiting(){
        //given
        OrderItemDto savedProduct1 = createOrderItemDto(1L, 1L, "상품1", "http://thumbnail1.jpg", 3, 3000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("사이즈").optionValueName("XL").build()));
        OrderItemDto savedProduct2 = createOrderItemDto(2L, 2L, "상품2", "http://thumbnail1.jpg", 5, 5000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("용량").optionValueName("256GB").build()));
        AppliedCoupon appliedCoupon = createAppliedCoupon(1L, "1000원 할인 쿠폰", 1000L);
        PaymentInfo paymentInfo = createPaymentInfo(34000, 3400, 1000, 1000, 28600);
        OrderDto orderDto = createOrderDto(1L, OrderStatus.PENDING, "상품1 외 1건", "서울시 테헤란로 123", paymentInfo, List.of(savedProduct1, savedProduct2), appliedCoupon,
                null);

        given(orderDomainService.getOrder(anyLong()))
                .willReturn(orderDto);
        //when
        //then
        assertThatThrownBy(() -> orderApplicationService.confirmOrder(1L, "paymentKey"))
                .isInstanceOf(OrderVerificationException.class)
                .hasMessage("결제 가능한 주문이 아닙니다");
    }

    @Test
    @DisplayName("결제를 승인할때 결제 승인이 실패한 경우 주문을 실패 처리, 주문 실패 이벤트를 발행하고 예외를 그대로 던진다")
    void confirmOrder_when_payment_fail(){
        //given
        OrderItemDto savedProduct1 = createOrderItemDto(1L, 1L, "상품1", "http://thumbnail1.jpg", 3, 3000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("사이즈").optionValueName("XL").build()));
        OrderItemDto savedProduct2 = createOrderItemDto(2L, 2L, "상품2", "http://thumbnail1.jpg", 5, 5000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("용량").optionValueName("256GB").build()));
        AppliedCoupon appliedCoupon = createAppliedCoupon(1L, "1000원 할인 쿠폰", 1000L);
        PaymentInfo paymentInfo = createPaymentInfo(34000, 3400, 1000, 1000, 28600);
        OrderDto paymentWaitingOrder = createOrderDto(1L, OrderStatus.PAYMENT_WAITING, "상품1 외 1건", "서울시 테헤란로 123", paymentInfo, List.of(savedProduct1, savedProduct2), appliedCoupon,
                null);
        OrderDto failureOrder = createOrderDto(1L, OrderStatus.CANCELED, "상품1 외 1건", "서울시 테헤란로 123",
                paymentInfo, List.of(savedProduct1, savedProduct2), appliedCoupon, OrderFailureCode.PAYMENT_FAILED);
        given(orderDomainService.getOrder(anyLong()))
                .willReturn(paymentWaitingOrder);
        willThrow(new PaymentException("결제 승인이 실패했습니다", PaymentErrorCode.APPROVAL_FAIL))
                .given(orderIntegrationService).confirmOrderPayment(anyLong(), anyString(), anyLong());
        given(orderDomainService.changeCanceled(anyLong(), any(OrderFailureCode.class)))
                .willReturn(failureOrder);
        //when
        //then
        assertThatThrownBy(() -> orderApplicationService.confirmOrder(1L, "paymentKey"))
                .isInstanceOf(PaymentException.class)
                .hasMessage("결제 승인이 실패했습니다");

        verify(orderDomainService, times(1)).changeCanceled(1L, OrderFailureCode.PAYMENT_FAILED);

        ArgumentCaptor<PaymentResultEvent> paymentCaptor = ArgumentCaptor.forClass(PaymentResultEvent.class);
        verify(eventPublisher, times(1)).publishEvent(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue())
                .extracting(PaymentResultEvent::getOrderId, PaymentResultEvent::getStatus, PaymentResultEvent::getCode, PaymentResultEvent::getFailureReason)
                .containsExactly(1L, OrderEventStatus.FAILURE, OrderEventCode.PAYMENT_AUTHORIZED_FAILED, "결제 승인이 실패했습니다");
    }

    @Test
    @DisplayName("주문을 조회한다")
    void getOrder(){
        //given
        Long orderId = 1L;
        Long userId = 1L;
        UserPrincipal userPrincipal = UserPrincipal.of(userId, UserRole.ROLE_USER);
        OrderItemDto savedProduct1 = createOrderItemDto(1L, 1L, "상품1", "http://thumbnail1.jpg", 3, 3000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("사이즈").optionValueName("XL").build()));
        OrderItemDto savedProduct2 = createOrderItemDto(2L, 2L, "상품2", "http://thumbnail2.jpg", 5, 5000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("용량").optionValueName("256GB").build()));
        AppliedCoupon appliedCoupon = createAppliedCoupon(1L, "1000원 할인 쿠폰", 1000L);
        PaymentInfo paymentInfo = createPaymentInfo(34000, 3400, 1000, 1000, 28600);
        OrderDto orderDto = createOrderDto(userId, OrderStatus.COMPLETED, "상품1 외 1건", "서울시 테헤란로 123", paymentInfo, List.of(savedProduct1, savedProduct2), appliedCoupon,
                null);        given(orderDomainService.getOrder(anyLong()))
                .willReturn(orderDto);
        //when
        OrderDetailResponse result = orderApplicationService.getOrder(userPrincipal, orderId);
        //then
        assertThat(result)
                .extracting(OrderDetailResponse::getOrderId, OrderDetailResponse::getUserId, OrderDetailResponse::getOrderStatus, OrderDetailResponse::getOrderName,
                        OrderDetailResponse::getDeliveryAddress)
                .containsExactly(1L, 1L, "COMPLETED", "상품1 외 1건", "서울시 테헤란로 123");

        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getPaymentResponse())
                .extracting(OrderDetailResponse.PaymentResponse::getTotalOriginPrice,
                        OrderDetailResponse.PaymentResponse::getTotalProductDiscount,
                        OrderDetailResponse.PaymentResponse::getCouponDiscount,
                        OrderDetailResponse.PaymentResponse::getPointDiscount,
                        OrderDetailResponse.PaymentResponse::getFinalPaymentAmount)
                .containsExactly(34000L, 3400L, 1000L, 1000L, 28600L);

        assertThat(result.getCouponResponse())
                .extracting(OrderDetailResponse.CouponResponse::getCouponId,
                        OrderDetailResponse.CouponResponse::getCouponName,
                        OrderDetailResponse.CouponResponse::getCouponDiscount)
                .containsExactly(1L, "1000원 할인 쿠폰", 1000L);

        assertThat(result.getOrderItems()).hasSize(2)
                .extracting(OrderItemResponse::getProductId,
                        OrderItemResponse::getProductVariantId,
                        OrderItemResponse::getProductName,
                        OrderItemResponse::getThumbNailUrl,
                        OrderItemResponse::getQuantity,
                        OrderItemResponse::getLineTotal)
                .contains(
                        tuple(1L, 1L, "상품1", "http://thumbnail1.jpg", 3, 8100L),
                        tuple(2L, 2L, "상품2", "http://thumbnail2.jpg", 5, 22500L)
                );

        assertThat(result.getOrderItems())
                .satisfiesExactlyInAnyOrder(
                        item1 -> {
                            assertThat(item1.getProductId()).isEqualTo(1L);
                            assertThat(item1.getProductVariantId()).isEqualTo(1L);
                            assertThat(item1.getProductName()).isEqualTo("상품1");
                            assertThat(item1.getThumbNailUrl()).isEqualTo("http://thumbnail1.jpg");
                            assertThat(item1.getQuantity()).isEqualTo(3);
                            assertThat(item1.getLineTotal()).isEqualTo(8100L);
                            assertThat(item1.getUnitPrice())
                                    .extracting(OrderItemResponse.OrderItemPrice::getOriginalPrice, OrderItemResponse.OrderItemPrice::getDiscountRate,
                                            OrderItemResponse.OrderItemPrice::getDiscountAmount, OrderItemResponse.OrderItemPrice::getDiscountedPrice)
                                    .contains(3000L, 10, 300L, 2700L);
                            assertThat(item1.getOptions())
                                    .extracting(OrderItemResponse.OrderItemOption::getOptionTypeName, OrderItemResponse.OrderItemOption::getOptionValueName)
                                    .containsExactlyInAnyOrder(
                                            tuple("사이즈", "XL")
                                    );
                        },
                        item2 -> {
                            assertThat(item2.getProductId()).isEqualTo(2L);
                            assertThat(item2.getProductVariantId()).isEqualTo(2L);
                            assertThat(item2.getProductName()).isEqualTo("상품2");
                            assertThat(item2.getThumbNailUrl()).isEqualTo("http://thumbnail2.jpg");
                            assertThat(item2.getQuantity()).isEqualTo(5);
                            assertThat(item2.getLineTotal()).isEqualTo(22500L);
                            assertThat(item2.getUnitPrice())
                                    .extracting(OrderItemResponse.OrderItemPrice::getOriginalPrice, OrderItemResponse.OrderItemPrice::getDiscountRate,
                                            OrderItemResponse.OrderItemPrice::getDiscountAmount, OrderItemResponse.OrderItemPrice::getDiscountedPrice)
                                    .contains(5000L, 10, 500L, 4500L);
                            assertThat(item2.getOptions())
                                    .extracting(OrderItemResponse.OrderItemOption::getOptionTypeName, OrderItemResponse.OrderItemOption::getOptionValueName)
                                    .containsExactlyInAnyOrder(
                                            tuple("용량", "256GB")
                                    );
                        }
                );
    }

    @Test
    @DisplayName("주문의 유저 아이디와 요청한 유저아이디가 일치하지 않는 경우 예외를 던진다")
    void getOrder_not_match_userId(){
        //given
        Long orderId = 1L;
        Long userId = 1L;
        UserPrincipal userPrincipal = UserPrincipal.of(2L, UserRole.ROLE_USER);
        OrderItemDto savedProduct1 = createOrderItemDto(1L, 1L, "상품1", "http://thumbnail1.jpg", 3, 3000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("사이즈").optionValueName("XL").build()));
        OrderItemDto savedProduct2 = createOrderItemDto(2L, 2L, "상품2", "http://thumbnail2.jpg", 5, 5000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("용량").optionValueName("256GB").build()));
        AppliedCoupon appliedCoupon = createAppliedCoupon(1L, "1000원 할인 쿠폰", 1000L);
        PaymentInfo paymentInfo = createPaymentInfo(34000, 3400, 1000, 1000, 28600);
        OrderDto orderDto = createOrderDto(userId, OrderStatus.COMPLETED, "상품1 외 1건", "서울시 테헤란로 123", paymentInfo, List.of(savedProduct1, savedProduct2), appliedCoupon,
                null);        given(orderDomainService.getOrder(anyLong()))
                .willReturn(orderDto);
        //when
        //then
        assertThatThrownBy(() -> orderApplicationService.getOrder(userPrincipal, orderId))
                .isInstanceOf(NoPermissionException.class)
                .hasMessage("주문을 조회할 권한이 없습니다");
    }

    private CreateOrderDto createOrderDto(UserPrincipal userPrincipal, String deliveryAddress, Long couponId, Long pointToUse,
                                          Long expectedPrice, CreateOrderItemDto... orderItems){
        return CreateOrderDto.builder()
                .userPrincipal(userPrincipal)
                .deliveryAddress(deliveryAddress)
                .couponId(couponId)
                .pointToUse(pointToUse)
                .expectedPrice(expectedPrice)
                .orderItemDtoList(List.of(orderItems))
                .build();
    }

    private CreateOrderItemDto createOrderItemDto(Long productVariantId, int quantity){
        return CreateOrderItemDto.builder()
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();
    }

    private UserPrincipal createUserPrincipal(Long userId, UserRole userRole){
        return UserPrincipal.builder()
                .userId(userId)
                .userRole(userRole)
                .build();
    }

    private OrderProductResponse createProductResponse(Long productId, Long productVariantId,
                                                      String productName, Long originalPrice, int discountRate,
                                                      String thumbnail, int stockQuantity, List<OrderProductResponse.ItemOption> options){
        long discountAmount = originalPrice * discountRate / 100;
        return OrderProductResponse.builder()
                .productId(productId)
                .productVariantId(productVariantId)
                .productName(productName)
                .unitPrice(
                        OrderProductResponse.UnitPrice.builder()
                                .originalPrice(originalPrice)
                                .discountRate(discountRate)
                                .discountAmount(discountAmount)
                                .discountedPrice(originalPrice - discountAmount)
                                .build())
                .thumbnailUrl(thumbnail)
                .stockQuantity(stockQuantity)
                .itemOptions(options)
                .build();
    }

    private OrderDto createOrderDto(Long userId, OrderStatus status, String orderName, String deliveryAddress,
                                    PaymentInfo paymentInfo, List<OrderItemDto> orderItemDtoList, AppliedCoupon appliedCoupon,
                                    OrderFailureCode failureCode){
        return OrderDto.builder()
                .orderId(1L)
                .userId(userId)
                .status(status)
                .orderName(orderName)
                .deliveryAddress(deliveryAddress)
                .orderedAt(LocalDateTime.now())
                .paymentInfo(paymentInfo)
                .orderItemDtoList(orderItemDtoList)
                .appliedCoupon(appliedCoupon)
                .orderFailureCode(failureCode)
                .build();
    }

    private PaymentInfo createPaymentInfo(long totalOriginPrice, long totalProductDiscount, long couponDiscount, long usedPoint,
                                          long finalPaymentAmount){
        return PaymentInfo.builder()
                .totalOriginPrice(totalOriginPrice)
                .totalProductDiscount(totalProductDiscount)
                .couponDiscount(couponDiscount)
                .usedPoint(usedPoint)
                .finalPaymentAmount(finalPaymentAmount)
                .build();
    }

    private AppliedCoupon createAppliedCoupon(Long couponId, String couponName, Long discountAmount){
        return AppliedCoupon.builder()
                .couponId(couponId)
                .couponName(couponName)
                .discountAmount(discountAmount)
                .build();
    }

    private OrderItemDto createOrderItemDto(Long productId, Long productVariantId, String productName, String thumbnailUrl,
                                            int quantity, long originPrice, int discountRate,
                                            List<OrderItemDto.ItemOptionDto> itemOptionDtos){
        long discountAmount = originPrice * discountRate / 100;
        return OrderItemDto.builder()
                .productId(productId)
                .productVariantId(productVariantId)
                .productName(productName)
                .thumbnailUrl(thumbnailUrl)
                .quantity(quantity)
                .unitPrice(
                        OrderItemDto.UnitPrice.builder()
                                .originalPrice(originPrice)
                                .discountRate(discountRate)
                                .discountAmount(discountAmount)
                                .discountedPrice(originPrice - discountAmount)
                                .build())
                .itemOptionDtos(itemOptionDtos)
                .lineTotal((originPrice - discountAmount)*quantity)
                .build();
    }
}

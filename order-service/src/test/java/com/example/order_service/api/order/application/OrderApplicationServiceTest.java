package com.example.order_service.api.order.application;

import com.example.order_service.api.common.dto.PageDto;
import com.example.order_service.api.common.exception.NoPermissionException;
import com.example.order_service.api.common.exception.OrderVerificationException;
import com.example.order_service.api.common.exception.PaymentErrorCode;
import com.example.order_service.api.common.exception.PaymentException;
import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.application.dto.result.OrderDetailResponse;
import com.example.order_service.api.order.application.dto.result.OrderItemResponse;
import com.example.order_service.api.order.application.dto.result.OrderListResponse;
import com.example.order_service.api.order.application.event.*;
import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
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
import com.example.order_service.api.support.fixture.OrderTestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.order_service.api.support.fixture.OrderTestFixture.*;
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
        given(orderDomainService.changeCanceled(orderId, failureCode))
                .willReturn(canceledOrder);
        //when
        orderApplicationService.changeCanceled(orderId, failureCode);
        //then
        verify(orderDomainService, times(1)).changeCanceled(orderId, failureCode);

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
        given(orderDomainService.changeOrderStatus(orderId, OrderStatus.COMPLETED))
                .willReturn(completedOrder);
        //when
        OrderDetailResponse result = orderApplicationService.confirmOrder(orderId, paymentKey);
        //then
        verify(orderDomainService, times(1)).changeOrderStatus(orderId, OrderStatus.COMPLETED);
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
                null);
        given(orderDomainService.getOrder(anyLong()))
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

    @Test
    @DisplayName("주문 목록을 조회한다")
    void getOrders() {
        //given
        Long userId = 1L;
        UserPrincipal userPrincipal = createUserPrincipal(userId, UserRole.ROLE_USER);
        OrderSearchCondition condition = OrderSearchCondition.builder()
                .page(1)
                .size(10)
                .sort("latest")
                .build();
        OrderItemDto savedProduct1 = createOrderItemDto(1L, 1L, "상품1", "http://thumbnail1.jpg", 3, 3000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("사이즈").optionValueName("XL").build()));
        OrderItemDto savedProduct2 = createOrderItemDto(2L, 2L, "상품2", "http://thumbnail2.jpg", 5, 5000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("용량").optionValueName("256GB").build()));
        AppliedCoupon appliedCoupon1 = createAppliedCoupon(1L, "1000원 할인 쿠폰", 1000L);
        AppliedCoupon appliedCoupon2 = createAppliedCoupon(2L, "1000원 할인 쿠폰", 1000L);
        PaymentInfo paymentInfo = createPaymentInfo(34000, 3400, 1000, 1000, 28600);
        OrderDto orderDto1 = createOrderDto(userId, OrderStatus.COMPLETED, "상품1 외 1건", "서울시 테헤란로 123", paymentInfo, List.of(savedProduct1, savedProduct2), appliedCoupon1,
                null);
        OrderDto orderDto2 = createOrderDto(userId, OrderStatus.COMPLETED, "상품1 외 1건", "서울시 테헤란로 123", paymentInfo, List.of(savedProduct1, savedProduct2), appliedCoupon2,
                null);
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt");
        Page<OrderDto> pageOrderDto = new PageImpl<>(
                List.of(orderDto1, orderDto2),
                pageable,
                100
        );
        given(orderDomainService.getOrders(anyLong(), any(OrderSearchCondition.class)))
                .willReturn(pageOrderDto);
        OrderListResponse orderListResponse1 = createOrderListResponse(orderDto1);
        OrderListResponse orderListResponse2 = createOrderListResponse(orderDto2);
        List<OrderListResponse> expected = List.of(orderListResponse1, orderListResponse2);

        //when
        PageDto<OrderListResponse> result = orderApplicationService.getOrders(userPrincipal, condition);
        //then
        assertThat(result)
                .extracting(PageDto::getCurrentPage, PageDto::getTotalPage, PageDto::getPageSize, PageDto::getTotalElement)
                .containsExactly(1, 10L, 10, 100L);

        assertThat(result.getContent())
                .usingRecursiveComparison()
                .ignoringFields("createdAt")
                .isEqualTo(expected);

    }

    private UserPrincipal createUserPrincipal(Long userId, UserRole userRole){
        return UserPrincipal.builder()
                .userId(userId)
                .userRole(userRole)
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

    private OrderListResponse createOrderListResponse(OrderDto orderDto){
        List<OrderItemResponse> list = orderDto.getOrderItemDtoList().stream().map(OrderItemResponse::from).toList();
        return OrderListResponse.builder()
                .orderId(orderDto.getOrderId())
                .userId(orderDto.getUserId())
                .orderStatus(orderDto.getStatus().name())
                .orderItems(list)
                .createdAt(orderDto.getOrderedAt().toString())
                .build();
    }
}

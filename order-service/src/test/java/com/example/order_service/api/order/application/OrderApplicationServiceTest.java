package com.example.order_service.api.order.application;

import com.example.order_service.api.common.exception.PaymentException;
import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.application.event.OrderCreatedEvent;
import com.example.order_service.api.order.application.event.OrderEventCode;
import com.example.order_service.api.order.application.event.OrderResultEvent;
import com.example.order_service.api.order.application.event.OrderEventStatus;
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
        AppliedCoupon appliedCoupon = createAppliedCoupon(1L, "1000원 할인 쿠폰");
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
                .extracting("status", "orderName", "finalPaymentAmount")
                .contains("PENDING", "상품1 외 1건", 28600L);
        assertThat(response.getCreateAt()).isNotNull();

        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        OrderCreatedEvent publishedEvent = captor.getValue();
        assertThat(publishedEvent)
                .extracting("orderId", "userId", "couponId", "usedPoint")
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
        AppliedCoupon appliedCoupon = createAppliedCoupon(1L, "1000원 할인 쿠폰");
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
        AppliedCoupon appliedCoupon = createAppliedCoupon(1L, "1000원 할인 쿠폰");
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
    @DisplayName("")
    void confirmOrder(){
        //given
        OrderItemDto savedProduct1 = createOrderItemDto(1L, 1L, "상품1", "http://thumbnail1.jpg", 3, 3000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("사이즈").optionValueName("XL").build()));
        OrderItemDto savedProduct2 = createOrderItemDto(2L, 2L, "상품2", "http://thumbnail1.jpg", 5, 5000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("용량").optionValueName("256GB").build()));
        AppliedCoupon appliedCoupon = createAppliedCoupon(1L, "1000원 할인 쿠폰");
        PaymentInfo paymentInfo = createPaymentInfo(34000, 3400, 1000, 1000, 28600);
        OrderDto orderDto = createOrderDto(1L, OrderStatus.PAYMENT_WAITING, "상품1 외 1건", "서울시 테헤란로 123", paymentInfo, List.of(savedProduct1, savedProduct2), appliedCoupon,
                null);
        //when
        //then
    }

    @Test
    @DisplayName("결제를 승인할때 주문 상태가 결제 대기 상태가 아니면 예외를 던진다")
    void confirmOrder_with_notPaymentWaiting(){
        //given
        OrderItemDto savedProduct1 = createOrderItemDto(1L, 1L, "상품1", "http://thumbnail1.jpg", 3, 3000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("사이즈").optionValueName("XL").build()));
        OrderItemDto savedProduct2 = createOrderItemDto(2L, 2L, "상품2", "http://thumbnail1.jpg", 5, 5000L, 10,
                List.of(OrderItemDto.ItemOptionDto.builder().optionTypeName("용량").optionValueName("256GB").build()));
        AppliedCoupon appliedCoupon = createAppliedCoupon(1L, "1000원 할인 쿠폰");
        PaymentInfo paymentInfo = createPaymentInfo(34000, 3400, 1000, 1000, 28600);
        OrderDto orderDto = createOrderDto(1L, OrderStatus.PENDING, "상품1 외 1건", "서울시 테헤란로 123", paymentInfo, List.of(savedProduct1, savedProduct2), appliedCoupon,
                null);

        given(orderDomainService.getOrder(anyLong()))
                .willReturn(orderDto);
        //when
        //then
        assertThatThrownBy(() -> orderApplicationService.confirmOrder(1L, "paymentKey"))
                .isInstanceOf(PaymentException.class)
                .hasMessage("주문이 결제 가능한 상태가 아닙니다");
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

    private AppliedCoupon createAppliedCoupon(Long couponId, String couponName){
        return AppliedCoupon.builder()
                .couponId(couponId)
                .couponName(couponName)
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
                .build();
    }
}

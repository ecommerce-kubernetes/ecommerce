package com.example.order_service.api.order.application;

import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.application.event.OrderCreatedEvent;
import com.example.order_service.api.order.domain.model.vo.AppliedCoupon;
import com.example.order_service.api.order.domain.model.vo.PaymentInfo;
import com.example.order_service.api.order.domain.service.OrderDomainService;
import com.example.order_service.api.order.domain.service.OrderPriceCalculator;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.OrderCreationResult;
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

import static org.assertj.core.api.Assertions.assertThat;
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
        OrderCreationResult orderCreationResult = createOrderCreationResult(1L, "상품1 외 1건", "서울시 테헤란로 123", paymentInfo, List.of(savedProduct1, savedProduct2), appliedCoupon);

        given(orderIntegrationService.getOrderUser(any(UserPrincipal.class))).willReturn(user);
        given(orderIntegrationService.getOrderProducts(anyList()))
                .willReturn(List.of(product1, product2));
        given(orderIntegrationService.getCoupon(any(UserPrincipal.class), anyLong(), anyLong()))
                .willReturn(coupon);
        given(orderDomainService.saveOrder(any(OrderCreationContext.class)))
                .willReturn(orderCreationResult);
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
        assertThat(publishedEvent.getUserId()).isEqualTo(1L);
        assertThat(publishedEvent.getOrderedVariantIds())
                .hasSize(2)
                .contains(1L, 2L);
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

    private OrderCreationResult createOrderCreationResult(Long userId, String orderName, String deliveryAddress, PaymentInfo paymentInfo, List<OrderItemDto> orderItemDtoList, AppliedCoupon appliedCoupon){
        return OrderCreationResult.builder()
                .orderId(1L)
                .userId(userId)
                .status("PENDING")
                .orderName(orderName)
                .deliveryAddress(deliveryAddress)
                .orderedAt(LocalDateTime.now())
                .paymentInfo(paymentInfo)
                .orderItemDtoList(orderItemDtoList)
                .appliedCoupon(appliedCoupon)
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

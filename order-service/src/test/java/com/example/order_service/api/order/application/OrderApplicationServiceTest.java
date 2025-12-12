package com.example.order_service.api.order.application;

import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.domain.service.OrderDomainService;
import com.example.order_service.api.order.domain.service.OrderPriceCalculator;
import com.example.order_service.api.order.domain.service.dto.result.PriceCalculateResult;
import com.example.order_service.api.order.infrastructure.OrderIntegrationService;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderApplicationServiceTest {

    @InjectMocks
    private OrderApplicationService orderApplicationService;
    @Mock
    private OrderIntegrationService orderIntegrationService;
    @Mock
    private OrderPriceCalculator calculator;
    @Mock
    private OrderDomainService orderDomainService;

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
        long subTotalPrice = 30600L;
        OrderCouponCalcResponse coupon = OrderCouponCalcResponse.builder()
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L)
                .build();
        PriceCalculateResult priceCalcResult = createPriceCalcResult(subTotalPrice, 28600, coupon, 1000L);

        given(orderIntegrationService.getOrderUser(any(UserPrincipal.class))).willReturn(user);
        given(orderIntegrationService.getOrderProducts(anyList()))
                .willReturn(List.of(product1, product2));
        given(calculator.calculateSubTotalPrice(anyList(), anyList()))
                .willReturn(subTotalPrice);
        given(orderIntegrationService.getCoupon(any(UserPrincipal.class), anyLong(), anyLong()))
                .willReturn(coupon);
        given(calculator.calculateFinalPrice(anyLong(), anyLong(), anyLong(), any(OrderUserResponse.class), any(OrderCouponCalcResponse.class)))
                .willReturn(priceCalcResult);
        //when
        CreateOrderResponse response = orderApplicationService.createOrder(createOrderDto);
        //then
        assertThat(response.getOrderId()).isNotNull();
        assertThat(response)
                .extracting("status", "message", "totalQuantity", "finalPaymentAmount")
                .contains("PENDING", "상품1 외 2건", 8, 29600L);
        assertThat(response.getCreateAt()).isNotNull();
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

    private PriceCalculateResult createPriceCalcResult(long subTotalPrice, long finalPaymentAmount, OrderCouponCalcResponse coupon,
                                                       long useToPoint){
        return PriceCalculateResult.builder()
                .subTotalPrice(subTotalPrice)
                .finalPaymentAmount(finalPaymentAmount)
                .coupon(coupon)
                .useToPoint(useToPoint)
                .build();
    }
}

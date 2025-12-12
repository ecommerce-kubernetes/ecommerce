package com.example.order_service.api.order.application;

import com.example.order_service.api.common.exception.InsufficientException;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.OrderVerificationException;
import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.domain.service.OrderDomainService;
import com.example.order_service.api.order.domain.service.OrderPriceCalculator;
import com.example.order_service.api.order.infrastructure.OrderIntegrationService;
import com.example.order_service.api.order.infrastructure.client.coupon.OrderCouponClientService;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import com.example.order_service.api.order.infrastructure.client.product.OrderProductClientService;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.OrderUserClientService;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        CreateOrderDto createOrderDto = createOrderDto(userPrincipal, "서울시 테헤란로 123", 1L, 300L,
                29600L, orderItem1, orderItem2);



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
                                                      String thumbnail, List<OrderProductResponse.ItemOption> options){
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
                .itemOptions(options)
                .build();
    }
}

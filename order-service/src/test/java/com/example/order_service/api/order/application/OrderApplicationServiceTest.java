package com.example.order_service.api.order.application;

import com.example.order_service.api.common.exception.InsufficientException;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.OrderVerificationException;
import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderApplicationServiceTest {

    @InjectMocks
    private OrderApplicationService orderApplicationService;
    @Mock
    private OrderProductClientService orderProductClientService;
    @Mock
    private OrderUserClientService orderUserClientService;
    @Mock
    private OrderCouponClientService orderCouponClientService;

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

    @Test
    @DisplayName("주문 생성시 사용포인트가 유저 포인트 잔액보다 많으면 예외를 던진다")
    void createOrder_When_NotEnoughPoint() {
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        CreateOrderItemDto orderItem1 = createOrderItemDto(1L, 3);
        CreateOrderItemDto orderItem2 = createOrderItemDto(2L, 5);

        CreateOrderDto createOrderDto = createOrderDto(userPrincipal, "서울시 테헤란로 123", 1L, 300L,
                29600L, orderItem1, orderItem2);

        OrderUserResponse userInfo = OrderUserResponse.builder()
                .userId(1L)
                .pointBalance(100L)
                .build();

        OrderProductResponse product1 = createProductResponse(1L, 1L, "상품1", 3000L, 10,
                "http://thumbnail.jpg",
                List.of(
                        OrderProductResponse.ItemOption.builder()
                                .optionTypeName("사이즈")
                                .optionValueName("XL")
                                .build()
                )
        );

        OrderProductResponse product2 = createProductResponse(1L, 1L, "상품1", 3000L, 10,
                "http://thumbnail.jpg",
                List.of(
                        OrderProductResponse.ItemOption.builder()
                                .optionTypeName("사이즈")
                                .optionValueName("XL")
                                .build()
                )
        );
        given(orderUserClientService.getUserForOrder(anyLong()))
                .willReturn(userInfo);
        given(orderProductClientService.getProducts(anyList()))
                .willReturn(List.of(product1, product2));
        //when
        //then
        assertThatThrownBy(() -> orderApplicationService.createOrder(createOrderDto))
                .isInstanceOf(InsufficientException.class)
                .hasMessage("포인트가 부족합니다");
    }
    
    @Test
    @DisplayName("주문을 생성할때 상품 서비스에서 요청 상품에 대한 모든 상품 정보가 오지 않으면 예외를 던진다")
    void createOrder_When_ProductClientService_Return_InCompleteResponse() {
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        CreateOrderItemDto orderItem1 = createOrderItemDto(1L, 3);
        CreateOrderItemDto orderItem2 = createOrderItemDto(2L, 5);

        CreateOrderDto createOrderDto = createOrderDto(userPrincipal, "서울시 테헤란로 123", 1L, 300L,
                29600L, orderItem1, orderItem2);

        OrderUserResponse userInfo = OrderUserResponse.builder()
                .userId(1L)
                .pointBalance(3000L)
                .build();
        given(orderUserClientService.getUserForOrder(anyLong()))
                .willReturn(userInfo);

        OrderProductResponse product = createProductResponse(1L, 1L, "상품1", 3000L, 10,
                "http://thumbnail.jpg",
                List.of(
                        OrderProductResponse.ItemOption.builder()
                                .optionTypeName("사이즈")
                                .optionValueName("XL")
                                .build()
                )
        );
        given(orderProductClientService.getProducts(anyList()))
                .willReturn(List.of(product));
        //when
        //then
        assertThatThrownBy(() -> orderApplicationService.createOrder(createOrderDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문 상품중 존재하지 않은 상품이 있습니다. missingIds=[2]");
    }
    
    @Test
    @DisplayName("주문을 생성할때 결제 예상 금액과 실제 결제 금액이 같지 않은 경우 OrderVerification 예외를 던진다")
    void createOrder_When_finalPaymentAmount_NotEqual_ExpectedPrice() {
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        CreateOrderItemDto orderItem1 = createOrderItemDto(1L, 3);
        CreateOrderItemDto orderItem2 = createOrderItemDto(2L, 5);

        CreateOrderDto createOrderDto = createOrderDto(userPrincipal, "서울시 테헤란로 123", 1L, 300L,
                30000L, orderItem1, orderItem2);

        OrderUserResponse userInfo = OrderUserResponse.builder()
                .userId(1L)
                .pointBalance(3000L)
                .build();

        OrderProductResponse product1 = createProductResponse(1L, 1L, "상품1", 3000L, 10,
                "http://thumbnail1.jpg",
                List.of(
                        OrderProductResponse.ItemOption.builder()
                                .optionTypeName("사이즈")
                                .optionValueName("XL")
                                .build()
                )
        );

        OrderProductResponse product2 = createProductResponse(2L, 2L, "상품2", 5000L, 10,
                "http://thumbnail2.jpg",
                List.of(
                        OrderProductResponse.ItemOption.builder()
                                .optionTypeName("용량")
                                .optionValueName("256GB")
                                .build()
                )
        );
        OrderCouponCalcResponse coupon = OrderCouponCalcResponse.builder()
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L)
                .build();

        given(orderUserClientService.getUserForOrder(anyLong()))
                .willReturn(userInfo);
        given(orderProductClientService.getProducts(anyList()))
                .willReturn(List.of(product1, product2));
        given(orderCouponClientService.calculateDiscount(anyLong(), anyLong(), anyLong()))
                .willReturn(coupon);
        //when
        //then
        assertThatThrownBy(() -> orderApplicationService.createOrder(createOrderDto))
                .isInstanceOf(OrderVerificationException.class)
                .hasMessage("주문 금액이 변동되었습니다");
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

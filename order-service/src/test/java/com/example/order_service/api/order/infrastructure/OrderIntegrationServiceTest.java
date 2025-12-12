package com.example.order_service.api.order.infrastructure;

import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderIntegrationServiceTest {

    @InjectMocks
    private OrderIntegrationService orderIntegrationService;

    @Mock
    private OrderProductClientService orderProductClientService;
    @Mock
    private OrderCouponClientService orderCouponClientService;
    @Mock
    private OrderUserClientService orderUserClientService;

    @Test
    @DisplayName("유저 정보를 가져온다")
    void getOrderUser() {
        //given
        UserPrincipal userPrincipal = UserPrincipal.builder().userId(1L)
                .userRole(UserRole.ROLE_USER)
                .build();

        OrderUserResponse user = OrderUserResponse.builder()
                .userId(1L)
                .pointBalance(1000L)
                .build();

        given(orderUserClientService.getUserForOrder(anyLong()))
                .willReturn(user);
        //when
        OrderUserResponse orderUser = orderIntegrationService.getOrderUser(userPrincipal);
        //then
        assertThat(orderUser)
                .extracting("userId", "pointBalance")
                .contains(1L, 1000L);
    }

    @Test
    @DisplayName("쿠폰 Id가 null 이 아니면 쿠폰 정보를 가져온다")
    void getCoupon() {
        //given
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .userId(1L)
                .userRole(UserRole.ROLE_USER)
                .build();

        OrderCouponCalcResponse coupon = OrderCouponCalcResponse.builder()
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L)
                .build();

        given(orderCouponClientService.calculateDiscount(anyLong(), anyLong(), anyLong()))
                .willReturn(coupon);

        //when
        OrderCouponCalcResponse result = orderIntegrationService.getCoupon(userPrincipal, 1L, 30000L);
        //then
        assertThat(result)
                .extracting("couponId", "couponName", "discountAmount")
                .contains(1L, "1000원 할인 쿠폰", 1000L);
    }

    @Test
    @DisplayName("쿠폰 id 가 null 이면 null을 반환한다")
    void getCoupon_When_CouponId_Is_Null() {
        //given
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .userId(1L)
                .userRole(UserRole.ROLE_USER)
                .build();
        //when
        OrderCouponCalcResponse coupon = orderIntegrationService.getCoupon(userPrincipal, null, 3000L);
        //then
        assertThat(coupon).isNull();
        verify(orderCouponClientService, never())
                .calculateDiscount(anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("주문한 상품 정보를 가져온다")
    void getOrderProducts() {
        //given
        CreateOrderItemDto orderItem1 = CreateOrderItemDto.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        CreateOrderItemDto orderItem2 = CreateOrderItemDto.builder()
                .productVariantId(2L)
                .quantity(5)
                .build();
        OrderProductResponse product1 = createProductResponse(1L, 1L, "상품1", 3000L, 10,
                "http://thumbnail1.jpg",
                List.of(OrderProductResponse
                        .ItemOption.builder()
                        .optionTypeName("사이즈")
                        .optionValueName("XL")
                        .build())
        );
        OrderProductResponse product2 = createProductResponse(2L, 2L, "상품2", 5000L, 10,
                "http://thumbnail2.jpg",
                List.of(OrderProductResponse
                        .ItemOption.builder()
                        .optionTypeName("용량")
                        .optionValueName("256GB")
                        .build())
        );
        List<CreateOrderItemDto> orderItems = List.of(orderItem1, orderItem2);
        List<OrderProductResponse> products = List.of(product1, product2);
        given(orderProductClientService.getProducts(anyList()))
                .willReturn(products);
        //when
        List<OrderProductResponse> result = orderIntegrationService.getOrderProducts(orderItems);
        //then
        assertThat(result).hasSize(2)
                .satisfiesExactlyInAnyOrder(
                        item1 -> {
                            assertThat(item1)
                                    .extracting("productId", "productVariantId", "productName", "thumbnailUrl")
                                    .contains(1L, 1L, "상품1", "http://thumbnail1.jpg");

                            assertThat(item1.getUnitPrice())
                                    .extracting("originalPrice", "discountRate", "discountAmount", "discountedPrice")
                                    .contains(3000L, 10, 300L, 2700L);

                            assertThat(item1.getItemOptions()).hasSize(1)
                                    .extracting("optionTypeName", "optionValueName")
                                    .containsExactlyInAnyOrder(
                                            tuple("사이즈", "XL")
                                    );
                        },
                        item2 -> {
                            assertThat(item2)
                                    .extracting("productId", "productVariantId", "productName", "thumbnailUrl")
                                    .contains(2L, 2L, "상품2", "http://thumbnail2.jpg");
                            assertThat(item2.getUnitPrice())
                                    .extracting("originalPrice", "discountRate", "discountAmount", "discountedPrice")
                                    .contains(5000L, 10, 500L, 4500L);

                            assertThat(item2.getItemOptions()).hasSize(1)
                                    .extracting("optionTypeName", "optionValueName")
                                    .containsExactlyInAnyOrder(
                                            tuple("용량", "256GB")
                                    );
                        }
                );
    }

    @Test
    @DisplayName("주문을 생성할때 상품 서비스에서 요청 상품에 대한 모든 상품 정보가 오지 않으면 예외를 던진다")
    void getOrderProducts_When_ProductClientService_Return_InCompleteResponse() {
        //given
        CreateOrderItemDto orderItem1 = CreateOrderItemDto.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        CreateOrderItemDto orderItem2 = CreateOrderItemDto.builder()
                .productVariantId(2L)
                .quantity(5)
                .build();

        OrderProductResponse product1 = createProductResponse(1L, 1L, "상품1", 3000L, 10,
                "http://thumbnail1.jpg",
                List.of(OrderProductResponse
                        .ItemOption.builder()
                        .optionTypeName("사이즈")
                        .optionValueName("XL")
                        .build())
        );

        List<CreateOrderItemDto> orderItems = List.of(orderItem1, orderItem2);
        List<OrderProductResponse> products = List.of(product1);
        given(orderProductClientService.getProducts(anyList()))
                .willReturn(products);
        //when
        //then
        assertThatThrownBy(() -> orderIntegrationService.getOrderProducts(orderItems))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문 상품중 존재하지 않은 상품이 있습니다. missingIds=[2]");
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

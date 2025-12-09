package com.example.order_service.api.order.application;

import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import com.example.order_service.api.common.exception.InsufficientException;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.server.InternalServerException;
import com.example.order_service.api.common.exception.server.UnavailableServiceException;
import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
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
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
public class OrderApplicationServiceTest {

    @InjectMocks
    private OrderApplicationService orderApplicationService;
    @Mock
    private OrderProductClientService orderProductClientService;
    @Mock
    private OrderUserClientService orderUserClientService;

    @Test
    @DisplayName("주문을 생성한다")
    void createOrder(){
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        CreateOrderItemDto orderItem1 = createOrderItemDto(1L, 3);
        CreateOrderItemDto orderItem2 = createOrderItemDto(2L, 5);

        CreateOrderDto createOrderDto = createOrderDto(userPrincipal, "서울시 테헤란로 123", 1L, 300L,
                5700L, orderItem1, orderItem2);
        //when
        CreateOrderResponse response = orderApplicationService.createOrder(createOrderDto);
        //then
        assertThat(response.getOrderId()).isNotNull();
        assertThat(response)
                .extracting("status", "message", "totalQuantity", "finalPaymentAmount")
                .contains("PENDING", "상품1 외 2건", 8, 5700L);
        assertThat(response.getCreateAt()).isNotNull();
    }

    @Test
    @DisplayName("주문을 생성할때 유저를 찾을 수 없으면 예외를 던진다")
    void createOrderWhenUserClientServiceThrownNotFoundException(){
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        CreateOrderItemDto orderItem1 = createOrderItemDto(1L, 3);
        CreateOrderItemDto orderItem2 = createOrderItemDto(2L, 5);

        CreateOrderDto createOrderDto = createOrderDto(userPrincipal, "서울시 테헤란로 123", 1L, 300L,
                5700L, orderItem1, orderItem2);

        willThrow(new NotFoundException("유저를 찾을 수 없습니다"))
                .given(orderUserClientService).getUserForOrder(anyLong());
        //when
        //then
        assertThatThrownBy(() -> orderApplicationService.createOrder(createOrderDto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("주문을 생성할때 유저 서비스가 응답하지 않으면 예외를 던진다")
    void createOrderWhenUserClientServiceThrownUnavailableException() {
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        CreateOrderItemDto orderItem1 = createOrderItemDto(1L, 3);
        CreateOrderItemDto orderItem2 = createOrderItemDto(2L, 5);

        CreateOrderDto createOrderDto = createOrderDto(userPrincipal, "서울시 테헤란로 123", 1L, 300L,
                5700L, orderItem1, orderItem2);

        willThrow(new UnavailableServiceException("유저 서비스가 응답하지 않습니다"))
                .given(orderUserClientService).getUserForOrder(anyLong());
        //when
        //then
        assertThatThrownBy(() -> orderApplicationService.createOrder(createOrderDto))
                .isInstanceOf(UnavailableServiceException.class)
                .hasMessage("유저 서비스가 응답하지 않습니다");
    }

    @Test
    @DisplayName("주문을 생성할때 유저 서비스에서 오류가 발생하면 예외를 던진다")
    void createOrderWhenUserClientServiceThrownInternalServerException() {
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        CreateOrderItemDto orderItem1 = createOrderItemDto(1L, 3);
        CreateOrderItemDto orderItem2 = createOrderItemDto(2L, 5);

        CreateOrderDto createOrderDto = createOrderDto(userPrincipal, "서울시 테헤란로 123", 1L, 300L,
                5700L, orderItem1, orderItem2);
        willThrow(new InternalServerException("유저 서비스에서 오류가 발생했습니다"))
                .given(orderUserClientService).getUserForOrder(anyLong());
        //when
        //then
        assertThatThrownBy(() -> orderApplicationService.createOrder(createOrderDto))
                .isInstanceOf(InternalServerException.class)
                .hasMessage("유저 서비스에서 오류가 발생했습니다");
    }

    @Test
    @DisplayName("주문 생성시 사용포인트가 유저 포인트 잔액보다 많으면 예외를 던진다")
    void createOrderWhenNotEnoughPoint() {
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        CreateOrderItemDto orderItem1 = createOrderItemDto(1L, 3);
        CreateOrderItemDto orderItem2 = createOrderItemDto(2L, 5);

        CreateOrderDto createOrderDto = createOrderDto(userPrincipal, "서울시 테헤란로 123", 1L, 300L,
                5700L, orderItem1, orderItem2);

        OrderUserResponse userInfo = OrderUserResponse.builder()
                .userId(1L)
                .pointBalance(100L)
                .build();
        given(orderUserClientService.getUserForOrder(anyLong()))
                .willReturn(userInfo);
        //when
        //then
        assertThatThrownBy(() -> orderApplicationService.createOrder(createOrderDto))
                .isInstanceOf(InsufficientException.class)
                .hasMessage("포인트가 부족합니다");
    }
    
    @Test
    @DisplayName("주문을 생성할때 상품 서비스에서 요청 상품에 대한 온전한 응답이 오지 않으면 예외를 던진다")
    void createOrderWhenProductClientServiceReturnInCompleteResponse() {
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        CreateOrderItemDto orderItem1 = createOrderItemDto(1L, 3);
        CreateOrderItemDto orderItem2 = createOrderItemDto(2L, 5);

        CreateOrderDto createOrderDto = createOrderDto(userPrincipal, "서울시 테헤란로 123", 1L, 300L,
                5700L, orderItem1, orderItem2);

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
    @DisplayName("주문 생성시 상품 서비스 서킷브레이커가 열리면 예외를 던진다")
    void createOrderWhenProductClientServiceThrownUnavailableServiceException() {
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        CreateOrderItemDto orderItem1 = createOrderItemDto(1L, 3);
        CreateOrderItemDto orderItem2 = createOrderItemDto(2L, 5);

        CreateOrderDto createOrderDto = createOrderDto(userPrincipal, "서울시 테헤란로 123", 1L, 300L,
                5700L, orderItem1, orderItem2);

        OrderUserResponse userInfo = OrderUserResponse.builder()
                .userId(1L)
                .pointBalance(3000L)
                .build();
        given(orderUserClientService.getUserForOrder(anyLong()))
                .willReturn(userInfo);
        willThrow(new UnavailableServiceException("상품 서비스가 응답하지 않습니다"))
                .given(orderProductClientService)
                .getProducts(anyList());
        //when
        //then
        assertThatThrownBy(() -> orderApplicationService.createOrder(createOrderDto))
                .isInstanceOf(UnavailableServiceException.class)
                .hasMessage("상품 서비스가 응답하지 않습니다");
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

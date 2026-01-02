package com.example.order_service.api.order.infrastructure;

import com.example.order_service.api.common.exception.InsufficientException;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.infrastructure.client.coupon.OrderCouponClientService;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;
import com.example.order_service.api.order.infrastructure.client.payment.TossPaymentClientService;
import com.example.order_service.api.order.infrastructure.client.payment.dto.TossPaymentConfirmResponse;
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

import static com.example.order_service.api.support.fixture.OrderExternalAdaptorFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderExternalAdaptorTest {

    @InjectMocks
    private OrderExternalAdaptor orderExternalAdaptor;

    @Mock
    private OrderProductClientService orderProductClientService;
    @Mock
    private OrderCouponClientService orderCouponClientService;
    @Mock
    private OrderUserClientService orderUserClientService;
    @Mock
    private TossPaymentClientService tossPaymentClientService;

    @Test
    @DisplayName("유저 정보를 가져온다")
    void getOrderUser() {
        //given
        given(orderUserClientService.getUserForOrder(anyLong()))
                .willReturn(createUserResponse());
        //when
        OrderUserResponse orderUser = orderExternalAdaptor.getOrderUser(USER_ID);
        //then
        assertThat(orderUser)
                .extracting(OrderUserResponse::getUserId, OrderUserResponse::getPointBalance)
                .contains(USER_ID, 10000L);
    }

    @Test
    @DisplayName("쿠폰 Id가 null 이 아니면 쿠폰 정보를 가져온다")
    void getCoupon() {
        //given
        given(orderCouponClientService.calculateDiscount(anyLong(), anyLong(), anyLong()))
                .willReturn(createCouponResponse());
        //when
        OrderCouponDiscountResponse result = orderExternalAdaptor.getCoupon(USER_ID, COUPON_ID, 30000L);
        //then
        assertThat(result)
                .extracting(OrderCouponDiscountResponse::getCouponId,
                        OrderCouponDiscountResponse::getCouponName,
                        OrderCouponDiscountResponse::getDiscountAmount)
                .contains(COUPON_ID, "1000원 할인 쿠폰", 1000L);
    }

    @Test
    @DisplayName("쿠폰 id 가 null 이면 null을 반환한다")
    void getCoupon_When_CouponId_Is_Null() {
        //given
        //when
        OrderCouponDiscountResponse coupon = orderExternalAdaptor.getCoupon(USER_ID, null, 3000L);
        //then
        assertThat(coupon).isNull();
        verify(orderCouponClientService, never())
                .calculateDiscount(anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("주문한 상품 정보를 가져온다")
    void getOrderProducts() {
        //given
        List<CreateOrderItemDto> requestItems = List.of(
                createOrderItemDto(VARIANT_ID_1, 3),
                createOrderItemDto(VARIANT_ID_2, 5)
        );
        List<OrderProductResponse> response = List.of(createProductResponse(VARIANT_ID_1, 100),
                createProductResponse(VARIANT_ID_2, 100)
        );
        given(orderProductClientService.getProducts(anyList())).willReturn(response);
        //when
        List<OrderProductResponse> result = orderExternalAdaptor.getOrderProducts(requestItems);
        //then
        assertThat(result).hasSize(2)
                .extracting(OrderProductResponse::getProductVariantId)
                .containsExactlyInAnyOrder(
                        VARIANT_ID_1, VARIANT_ID_2
                );
    }

    @Test
    @DisplayName("주문을 생성할때 상품 서비스에서 요청 상품에 대한 모든 상품 정보가 오지 않으면 예외를 던진다")
    void getOrderProducts_When_ProductClientService_Return_InCompleteResponse() {
        //given
        List<CreateOrderItemDto> requestItems = List.of(
                createOrderItemDto(VARIANT_ID_1, 3),
                createOrderItemDto(VARIANT_ID_2, 5)
        );
        List<OrderProductResponse> response = List.of(
                createProductResponse(VARIANT_ID_1, 100)
        );
        given(orderProductClientService.getProducts(anyList()))
                .willReturn(response);
        //when
        //then
        assertThatThrownBy(() -> orderExternalAdaptor.getOrderProducts(requestItems))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문 상품중 존재하지 않은 상품이 있습니다. missingIds=[20]");
    }
    
    @Test
    @DisplayName("주문을 생성할때 상품 재고가 주문 수량보다 부족하면 예외를 던진다")
    void getOrderProducts_When_OutCreateOrderStock() {
        //given
        List<CreateOrderItemDto> requestItems = List.of(
                createOrderItemDto(VARIANT_ID_1, 3),
                createOrderItemDto(VARIANT_ID_2, 5)
        );
        List<OrderProductResponse> response = List.of(
                createProductResponse(VARIANT_ID_1, 100),
                createProductResponse(VARIANT_ID_2,3)
        );
        given(orderProductClientService.getProducts(anyList()))
                .willReturn(response);
        //when
        //then
        assertThatThrownBy(() -> orderExternalAdaptor.getOrderProducts(requestItems))
                .isInstanceOf(InsufficientException.class)
                .hasMessage("재고가 부족합니다 (ProductVariantId: 20 | 현재 재고: 3 | 요청 수량: 5)");
    }

    @Test
    @DisplayName("토스에 결제 승인을 요청한다")
    void confirmOrderPayment(){
        //given
        given(tossPaymentClientService.confirmPayment(anyLong(), anyString(), anyLong()))
                .willReturn(createPaymentResponse());
        //when
        TossPaymentConfirmResponse tossPaymentConfirmResponse = orderExternalAdaptor.confirmOrderPayment(ORDER_ID, PAYMENT_KEY, 1000L);
        //then
        assertThat(tossPaymentConfirmResponse)
                .extracting(TossPaymentConfirmResponse::getPaymentKey, TossPaymentConfirmResponse::getOrderId,
                        TossPaymentConfirmResponse::getTotalAmount, TossPaymentConfirmResponse::getStatus)
                .containsExactly(PAYMENT_KEY, ORDER_ID, 10000L, "DONE");
    }
}

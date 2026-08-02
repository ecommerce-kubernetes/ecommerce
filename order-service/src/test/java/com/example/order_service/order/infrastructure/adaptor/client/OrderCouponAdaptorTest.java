package com.example.order_service.order.infrastructure.adaptor.client;

import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.common.exception.port.CouponPortErrorCode;
import com.example.order_service.common.exception.port.DefaultPortException;
import com.example.order_service.infrastructure.dto.response.coupon.CartCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponsResponse;
import com.example.order_service.infrastructure.gateway.CouponGateway;
import com.example.order_service.order.application.port.dto.CartCouponResult;
import com.example.order_service.order.application.port.dto.ItemCouponsResult;
import com.example.order_service.order.infrastructure.adaptor.mapper.OrderCouponPortMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderCouponAdaptorTest {

    @InjectMocks
    private OrderCouponAdaptor orderCouponAdaptor;
    @Mock
    private CouponGateway gateway;
    @Spy
    private OrderCouponPortMapper orderCouponPortMapper;

    @Test
    @DisplayName("상품 쿠폰 정보를 조회한다.")
    void getItemCoupons(){
        //given
        Long userId = 1L;
        List<Long> itemCouponIds = List.of(1L, 2L);

        ItemCouponsResponse.ItemCoupon fixedCoupon = ItemCouponsResponse.ItemCoupon.builder()
                .itemCouponId(1L)
                .status("AVAILABLE")
                .name("청바지 1000원 할인 쿠폰")
                .applyQuantityLimit(1)
                .discountType("FIXED")
                .discountAmount(1000L)
                .expiresAt(LocalDateTime.now())
                .build();

        ItemCouponsResponse.ItemCoupon rateCoupon = ItemCouponsResponse.ItemCoupon.builder()
                .itemCouponId(2L)
                .status("AVAILABLE")
                .name("반팔티 10% 할인 쿠폰")
                .applyQuantityLimit(1)
                .discountType("RATE")
                .discountRate(10)
                .maxDiscountAmount(10000L)
                .expiresAt(LocalDateTime.now())
                .build();

        ItemCouponsResponse response = ItemCouponsResponse.builder()
                .userId(1L)
                .itemCoupons(List.of(fixedCoupon, rateCoupon))
                .build();

        given(gateway.getItemCoupons(anyLong(), anyList())).willReturn(response);
        //when
        ItemCouponsResult itemCoupons = orderCouponAdaptor.getItemCoupons(userId, itemCouponIds);
        //then
        assertThat(itemCoupons.itemCoupons()).hasSize(2);
    }

    @Test
    @DisplayName("상품 쿠폰 조회중 쿠폰 서비스에서 서버 오류가 발생한 경우 예외가 발생한다.")
    void getItemCoupons_ExternalServerException(){
        //given
        Long userId = 1L;
        List<Long> itemCouponIds = List.of(1L, 2L);
        String code = "INTERNAL_ERROR";
        String message = "알 수 없는 에러가 발생했습니다.";
        given(gateway.getItemCoupons(anyLong(), anyList())).willThrow(new ExternalServerException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponAdaptor.getItemCoupons(userId, itemCouponIds))
                .isInstanceOf(DefaultPortException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CouponPortErrorCode.COUPON_SERVER_ERROR, code);
    }

    @Test
    @DisplayName("상품 쿠폰 조회중 쿠폰 서비스에서 클라이언트 오류가 발생한 경우 예외가 발생한다.")
    void getItemCoupons_ExternalClientException(){
        //given
        Long userId = 1L;
        List<Long> itemCouponIds = List.of(1L, 2L);
        String code = "PERMISSION_DENIED";
        String message = "조회 권한이 없습니다.";
        given(gateway.getItemCoupons(anyLong(), anyList())).willThrow(new ExternalClientException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponAdaptor.getItemCoupons(userId, itemCouponIds))
                .isInstanceOf(DefaultPortException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CouponPortErrorCode.COUPON_CLIENT_ERROR, code);
    }

    @Test
    @DisplayName("상품 쿠폰 조회중 쿠폰 서비스 서킷 브레이커가 열린 경우 예외가 발생한다")
    void getItemCoupons_ExternalCircuitBreakerException(){
        //given
        Long userId = 1L;
        List<Long> itemCouponIds = List.of(1L, 2L);
        String code = "COUPON_CIRCUIT_OPEN";
        String message = "쿠폰 서비스 서킷 브레이커 열림";
        given(gateway.getItemCoupons(anyLong(), anyList())).willThrow(new ExternalCircuitBreakerException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponAdaptor.getItemCoupons(userId, itemCouponIds))
                .isInstanceOf(DefaultPortException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CouponPortErrorCode.COUPON_CIRCUIT_OPEN, code);
    }

    @Test
    @DisplayName("상품 쿠폰 조회중 쿠폰 서비스에서 통신 불가 오류가 발생한 경우 예외가 발생한다")
    void getItemCoupons_ExternalUnavailableServerException(){
        //given
        Long userId = 1L;
        List<Long> itemCouponIds = List.of(1L, 2L);
        String code = "SERVICE_UNAVAILABLE";
        String message = "쿠폰 서비스 통신 장애";
        given(gateway.getItemCoupons(anyLong(), anyList())).willThrow(new ExternalSystemUnavailableException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponAdaptor.getItemCoupons(userId, itemCouponIds))
                .isInstanceOf(DefaultPortException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CouponPortErrorCode.COUPON_UNAVAILABLE_SERVER_ERROR, code);
    }

    @Test
    @DisplayName("장바구니 쿠폰을 조회한다")
    void getCartCoupon_fixed() {
        //given
        Long userId = 1L;
        Long cartCouponId = 20L;

        CartCouponResponse response = CartCouponResponse.builder()
                .userId(userId)
                .cartCouponId(cartCouponId)
                .name("장바구니 1000원 할인 쿠폰")
                .minimumPaymentAmount(50000L)
                .discountType("FIXED")
                .discountAmount(1000L)
                .build();

        given(gateway.getCartCoupon(anyLong(), anyLong()))
                .willReturn(response);

        //when
        CartCouponResult result = orderCouponAdaptor.getCartCoupon(userId, cartCouponId);
        //then
        assertThat(result.cartCoupon()).isNotNull();
    }

    @Test
    @DisplayName("장바구니 쿠폰 쿠폰 조회중 쿠폰 서비스에서 서버 오류가 발생한 경우 예외가 발생한다")
    void getCartCoupon_ExternalServerException(){
        //given
        String code = "INTERNAL_SERVER_ERROR";
        String message = "처리중 오류가 발생했습니다";
        given(gateway.getCartCoupon(anyLong(), anyLong())).willThrow(new ExternalServerException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponAdaptor.getCartCoupon(1L, 1L))
                .isInstanceOf(DefaultPortException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CouponPortErrorCode.COUPON_SERVER_ERROR, code);
    }

    @Test
    @DisplayName("장바구니 쿠폰 조회중 쿠폰 서비스에서 클라이언트 오류가 발생한 경우 예외가 발생한다")
    void getCartCoupon_ExternalClientException(){
        //given
        String code = "COUPON_EXPIRED";
        String message = "쿠폰이 만료되었습니다";
        given(gateway.getCartCoupon(anyLong(), anyLong())).willThrow(new ExternalClientException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponAdaptor.getCartCoupon(1L, 1L))
                .isInstanceOf(DefaultPortException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CouponPortErrorCode.COUPON_CLIENT_ERROR, code);
    }

    @Test
    @DisplayName("장바구니 쿠폰 조회중 쿠폰 서비스 서킷 브레이커가 열린 경우 예외가 발생한다")
    void getCartCoupon_ExternalCircuitBreakerException(){
        //given
        String code = "COUPON_CIRCUIT_OPEN";
        String message = "쿠폰 서비스 서킷 브레이커 열림";
        given(gateway.getCartCoupon(anyLong(), anyLong())).willThrow(new ExternalCircuitBreakerException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponAdaptor.getCartCoupon(1L, 1L))
                .isInstanceOf(DefaultPortException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CouponPortErrorCode.COUPON_CIRCUIT_OPEN, code);
    }

    @Test
    @DisplayName("장바구니 쿠폰 조회중 쿠폰 서비스에서 통신 불가 오류가 발생한 경우 예외가 발생한다")
    void getCartCoupon_ExternalUnavailableServerException() {
        //given
        String code = "SERVICE_UNAVAILABLE";
        String message = "쿠폰 서비스 통신 장애";
        given(gateway.getCartCoupon(anyLong(), anyLong())).willThrow(new ExternalSystemUnavailableException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponAdaptor.getCartCoupon(1L, 1L))
                .isInstanceOf(DefaultPortException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CouponPortErrorCode.COUPON_UNAVAILABLE_SERVER_ERROR, code);
    }
}

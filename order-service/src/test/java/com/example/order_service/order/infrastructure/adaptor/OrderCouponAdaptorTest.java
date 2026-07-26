package com.example.order_service.order.infrastructure.adaptor;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.common.exception.gateway.CouponGatewayErrorCode;
import com.example.order_service.common.exception.gateway.DefaultGatewayException;
import com.example.order_service.infrastructure.dto.response.coupon.CartCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponResponse;
import com.example.order_service.infrastructure.gateway.CouponGateway;
import com.example.order_service.order.application.port.dto.result.CartCouponResult;
import com.example.order_service.order.application.port.dto.result.ItemCouponResult;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.policy.RateCouponDiscountPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderCouponAdaptorTest {

    @InjectMocks
    private OrderCouponAdaptor orderCouponAdaptor;
    @Mock
    private CouponGateway adaptor;

    @Test
    @DisplayName("상품 쿠폰을 조회한다. (정액 할인 쿠폰)")
    void getItemCoupon_fixDiscount(){
        //given
        Long userId = 1L;
        Long itemCouponId = 1L;

        ItemCouponResponse response = ItemCouponResponse.builder()
                .userId(userId)
                .discountType("FIXED")
                .itemCouponId(itemCouponId)
                .name("청바지 1000원 할인")
                .applyQuantityLimit(1)
                .discountAmount(1000L)
                .build();

        given(adaptor.getItemCoupon(anyLong(), anyLong())).willReturn(response);

        //when
        ItemCouponResult result = orderCouponAdaptor.getItemCoupon(userId, itemCouponId);
        //then
        assertThat(result.itemCoupon())
                .extracting("itemCouponId", "name", "applyQuantityLimit")
                .containsExactly(itemCouponId, response.name(), response.applyQuantityLimit());

        assertThat(result.itemCoupon().getDiscountPolicy())
                .isExactlyInstanceOf(FixedCouponDiscountPolicy.class);
    }

    @Test
    @DisplayName("상품 쿠폰을 조회한다. (정률 할인 쿠폰)")
    void getItemCoupon_rateDiscount() {
        //given
        Long userId = 1L;
        Long itemCouponId = 1L;

        ItemCouponResponse response = ItemCouponResponse.builder()
                .userId(userId)
                .itemCouponId(itemCouponId)
                .name("청바지 10% 할인")
                .applyQuantityLimit(1)
                .discountType("RATE")
                .discountAmount(null)
                .discountRate(10)
                .maxDiscountAmount(50000L)
                .build();

        given(adaptor.getItemCoupon(anyLong(), anyLong())).willReturn(response);
        //when
        ItemCouponResult result = orderCouponAdaptor.getItemCoupon(userId, itemCouponId);
        //then
        assertThat(result.itemCoupon())
                .extracting("itemCouponId", "name", "applyQuantityLimit")
                .containsExactly(itemCouponId, response.name(), response.applyQuantityLimit());

        assertThat(result.itemCoupon().getDiscountPolicy())
                .isExactlyInstanceOf(RateCouponDiscountPolicy.class);
    }

    @Test
    @DisplayName("상품 쿠폰 조회중 쿠폰 서비스에서 서버 오류가 발생한 경우 예외가 발생한다")
    void getItemCoupon_ExternalServerException(){
        //given
        String code = "INTERNAL_SERVER_ERROR";
        String message = "처리중 오류가 발생했습니다";
        given(adaptor.getItemCoupon(anyLong(), anyLong())).willThrow(new ExternalServerException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponAdaptor.getItemCoupon(1L, 1L))
                .isInstanceOf(DefaultGatewayException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CouponGatewayErrorCode.COUPON_SERVER_ERROR, code);
    }

    @Test
    @DisplayName("쿠폰 조회중 쿠폰 서비스에서 클라이언트 오류가 발생한 경우 예외가 발생한다")
    void getItemCoupon_ExternalClientException(){
        //given
        String code = "COUPON_EXPIRED";
        String message = "쿠폰이 만료되었습니다";
        given(adaptor.getItemCoupon(anyLong(), anyLong())).willThrow(new ExternalClientException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponAdaptor.getItemCoupon(1L, 1L))
                .isInstanceOf(DefaultGatewayException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CouponGatewayErrorCode.COUPON_CLIENT_ERROR, code);
    }

    @Test
    @DisplayName("상품 쿠폰 조회중 쿠폰 서비스 서킷 브레이커가 열린 경우 예외가 발생한다")
    void getItemCoupon_ExternalCircuitBreakerException(){
        //given
        String code = "COUPON_CIRCUIT_OPEN";
        String message = "쿠폰 서비스 서킷 브레이커 열림";
        given(adaptor.getItemCoupon(anyLong(), anyLong())).willThrow(new ExternalCircuitBreakerException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponAdaptor.getItemCoupon(1L, 1L))
                .isInstanceOf(DefaultGatewayException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CouponGatewayErrorCode.COUPON_CIRCUIT_OPEN, code);
    }

    @Test
    @DisplayName("상품 쿠폰 조회중 쿠폰 서비스에서 통신 불가 오류가 발생한 경우 예외가 발생한다")
    void getItemCoupon_ExternalUnavailableServerException() {
        //given
        String code = "SERVICE_UNAVAILABLE";
        String message = "쿠폰 서비스 통신 장애";
        given(adaptor.getItemCoupon(anyLong(), anyLong())).willThrow(new ExternalSystemUnavailableException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponAdaptor.getItemCoupon(1L, 1L))
                .isInstanceOf(DefaultGatewayException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CouponGatewayErrorCode.COUPON_UNAVAILABLE_SERVER_ERROR, code);
    }

    @Test
    @DisplayName("장바구니 쿠폰을 조회한다 (정액 할인 쿠폰)")
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

        given(adaptor.getCartCoupon(anyLong(), anyLong()))
                .willReturn(response);

        //when
        CartCouponResult result = orderCouponAdaptor.getCartCoupon(userId, cartCouponId);
        //then
        assertThat(result.cartCoupon().getCartCouponId()).isEqualTo(20L);
        assertThat(result.cartCoupon().getName()).isEqualTo("장바구니 1000원 할인 쿠폰");
        assertThat(result.cartCoupon().getMinimumPaymentAmount()).isEqualTo(Money.wons(50000L));

        assertThat(result.cartCoupon().getDiscountPolicy())
                .isExactlyInstanceOf(FixedCouponDiscountPolicy.class);
    }

    @Test
    @DisplayName("장바구니 쿠폰을 조회한다 (정률 할인 쿠폰)")
    void getCartCoupon_rate() {
        //given
        Long userId = 1L;
        Long cartCouponId = 20L;

        CartCouponResponse response = CartCouponResponse.builder()
                .userId(userId)
                .cartCouponId(cartCouponId)
                .name("장바구니 5% 할인 쿠폰")
                .minimumPaymentAmount(50000L)
                .discountType("RATE")
                .discountRate(5)
                .maxDiscountAmount(10000L)
                .build();

        given(adaptor.getCartCoupon(anyLong(), anyLong())).willReturn(response);
        //when
        CartCouponResult result = orderCouponAdaptor.getCartCoupon(userId, cartCouponId);
        //then
        assertThat(result.cartCoupon().getCartCouponId()).isEqualTo(20L);
        assertThat(result.cartCoupon().getName()).isEqualTo("장바구니 5% 할인 쿠폰");
        assertThat(result.cartCoupon().getMinimumPaymentAmount()).isEqualTo(Money.wons(50000L));

        assertThat(result.cartCoupon().getDiscountPolicy())
                .isExactlyInstanceOf(RateCouponDiscountPolicy.class);
    }

    @Test
    @DisplayName("장바구니 쿠폰 쿠폰 조회중 쿠폰 서비스에서 서버 오류가 발생한 경우 예외가 발생한다")
    void getCartCoupon_ExternalServerException(){
        //given
        String code = "INTERNAL_SERVER_ERROR";
        String message = "처리중 오류가 발생했습니다";
        given(adaptor.getCartCoupon(anyLong(), anyLong())).willThrow(new ExternalServerException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponAdaptor.getCartCoupon(1L, 1L))
                .isInstanceOf(DefaultGatewayException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CouponGatewayErrorCode.COUPON_SERVER_ERROR, code);
    }

    @Test
    @DisplayName("장바구니 쿠폰 조회중 쿠폰 서비스에서 클라이언트 오류가 발생한 경우 예외가 발생한다")
    void getCartCoupon_ExternalClientException(){
        //given
        String code = "COUPON_EXPIRED";
        String message = "쿠폰이 만료되었습니다";
        given(adaptor.getCartCoupon(anyLong(), anyLong())).willThrow(new ExternalClientException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponAdaptor.getCartCoupon(1L, 1L))
                .isInstanceOf(DefaultGatewayException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CouponGatewayErrorCode.COUPON_CLIENT_ERROR, code);
    }

    @Test
    @DisplayName("장바구니 쿠폰 조회중 쿠폰 서비스 서킷 브레이커가 열린 경우 예외가 발생한다")
    void getCartCoupon_ExternalCircuitBreakerException(){
        //given
        String code = "COUPON_CIRCUIT_OPEN";
        String message = "쿠폰 서비스 서킷 브레이커 열림";
        given(adaptor.getCartCoupon(anyLong(), anyLong())).willThrow(new ExternalCircuitBreakerException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponAdaptor.getCartCoupon(1L, 1L))
                .isInstanceOf(DefaultGatewayException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CouponGatewayErrorCode.COUPON_CIRCUIT_OPEN, code);
    }

    @Test
    @DisplayName("장바구니 쿠폰 조회중 쿠폰 서비스에서 통신 불가 오류가 발생한 경우 예외가 발생한다")
    void getCartCoupon_ExternalUnavailableServerException() {
        //given
        String code = "SERVICE_UNAVAILABLE";
        String message = "쿠폰 서비스 통신 장애";
        given(adaptor.getCartCoupon(anyLong(), anyLong())).willThrow(new ExternalSystemUnavailableException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponAdaptor.getCartCoupon(1L, 1L))
                .isInstanceOf(DefaultGatewayException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CouponGatewayErrorCode.COUPON_UNAVAILABLE_SERVER_ERROR, code);
    }
}

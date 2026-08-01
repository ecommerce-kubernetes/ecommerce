package com.example.order_service.infrastructure.gateway;

import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.CouponFeignClient;
import com.example.order_service.infrastructure.dto.request.ItemCouponsRequest;
import com.example.order_service.infrastructure.dto.response.coupon.CartCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponsResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@IsolatedTest
public class CouponGatewayTest {
    @Autowired
    private CouponGateway couponGateway;
    @MockitoBean
    private CouponFeignClient client;
    @MockitoBean
    private ExternalExceptionTranslator translator;

    @Test
    @DisplayName("쿠폰 서비스에 쿠폰 할인 정보를 조회한다")
    void getItemCoupon(){
        //given
        ItemCouponResponse mockResponse = Instancio.create(ItemCouponResponse.class);
        given(client.getItemCoupon(anyLong(), anyLong()))
                .willReturn(mockResponse);
        //when
        ItemCouponResponse response = couponGateway.getItemCoupon(anyLong(), anyLong());
        //then
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("쿠폰 서비스 조회에서 예외 발생시 translator를 호출하여 반환된 예외를 던진다")
    void getItemCoupon_fallback_delegate_to_translator() throws Throwable {
        //given
        RuntimeException feignException = new RuntimeException("feignClient 예외");

        ExternalSystemUnavailableException translatedException =
                new ExternalSystemUnavailableException("CODE", "변환된 에러", feignException);

        given(client.getItemCoupon(anyLong(), anyLong())).willThrow(feignException);

        given(translator.translate(anyString(), any(Throwable.class)))
                .willReturn(translatedException);
        //when
        //then
        assertThatThrownBy(() -> couponGateway.getItemCoupon(1L, 1L))
                .isInstanceOf(ExternalSystemUnavailableException.class);
    }

    @Test
    @DisplayName("쿠폰 서비스에서 쿠폰 정보를 조회한다.")
    void getItemCoupons(){
        //given
        Long userId = 1L;
        List<Long> itemCouponIds = List.of(1L, 2L);
        ItemCouponsResponse mockResponse = Instancio.create(ItemCouponsResponse.class);
        given(client.getItemCoupons(anyLong(), any(ItemCouponsRequest.class))).willReturn(mockResponse);
        //when
        ItemCouponsResponse response = couponGateway.getItemCoupons(userId, itemCouponIds);
        //then
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("상품 쿠폰 정보를 조회중 예외 발생시 translator를 호출하여 반환된 예외를 던진다")
    void getItemCoupons_fallback_delegate_to_translator() throws Throwable {
        //given
        RuntimeException feignException = new RuntimeException("feignClient 예외");

        ExternalSystemUnavailableException translatedException =
                new ExternalSystemUnavailableException("CODE", "변환된 에러", feignException);

        given(client.getItemCoupons(anyLong(), any(ItemCouponsRequest.class))).willThrow(feignException);

        given(translator.translate(anyString(), any(Throwable.class)))
                .willReturn(translatedException);
        //when
        //then
        assertThatThrownBy(() -> couponGateway.getItemCoupons(anyLong(), anyList()))
                .isInstanceOf(ExternalSystemUnavailableException.class);
    }

    @Test
    @DisplayName("장바구니 쿠폰 할인 정보를 조회한다")
    void getCartCoupon() {
        //given
        CartCouponResponse mockResponse = Instancio.create(CartCouponResponse.class);
        given(client.getCartCoupon(anyLong(), anyLong()))
                .willReturn(mockResponse);
        //when
        CartCouponResponse response = couponGateway.getCartCoupon(1L, 1L);
        //then
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("쿠폰 서비스 조회에서 예외 발생시 translator를 호출하여 반환된 예외를 던진다")
    void getCartCoupon_fallback_delegate_to_translator() throws Throwable {
        //given
        RuntimeException feignException = new RuntimeException("feignClient 예외");

        ExternalSystemUnavailableException translatedException =
                new ExternalSystemUnavailableException("CODE", "변환된 에러", feignException);

        given(client.getCartCoupon(anyLong(), anyLong())).willThrow(feignException);

        given(translator.translate(anyString(), any(Throwable.class)))
                .willReturn(translatedException);
        //when
        //then
        assertThatThrownBy(() -> couponGateway.getCartCoupon(1L, 1L))
                .isInstanceOf(ExternalSystemUnavailableException.class);
    }
}

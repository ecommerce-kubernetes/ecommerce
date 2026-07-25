package com.example.order_service.infrastructure.gateway;

import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.CouponFeignClient;
import com.example.order_service.infrastructure.dto.response.coupon.CartCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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

        given(couponGateway.getItemCoupon(anyLong(), anyLong())).willThrow(feignException);

        given(translator.translate(anyString(), any(Throwable.class)))
                .willReturn(translatedException);
        //when
        //then
        assertThatThrownBy(() -> couponGateway.getItemCoupon(1L, 1L))
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

        given(couponGateway.getCartCoupon(anyLong(), anyLong())).willThrow(feignException);

        given(translator.translate(anyString(), any(Throwable.class)))
                .willReturn(translatedException);
        //when
        //then
        assertThatThrownBy(() -> couponGateway.getCartCoupon(1L, 1L))
                .isInstanceOf(ExternalSystemUnavailableException.class);
    }
}

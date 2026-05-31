package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.CouponFeignClient;
import com.example.order_service.infrastructure.dto.command.CouponCommand;
import com.example.order_service.infrastructure.dto.request.CouponClientRequest;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@IsolatedTest
public class CouponAdaptorTest {
    @Autowired
    private CouponAdaptor couponAdaptor;
    @MockitoBean
    private CouponFeignClient client;
    @MockitoBean
    private ExternalExceptionTranslator translator;

    @Test
    @DisplayName("쿠폰 서비스에 쿠폰 할인 정보를 조회한다")
    void calculate(){
        //given
        CouponCommand.Calculate command = fixtureMonkey.giveMeOne(CouponCommand.Calculate.class);
        CouponClientResponse.Calculate mockResponse = fixtureMonkey.giveMeOne(CouponClientResponse.Calculate.class);
        given(client.calculate(any(CouponClientRequest.Calculate.class)))
                .willReturn(mockResponse);
        //when
        CouponClientResponse.Calculate response = couponAdaptor.calculate(command);
        //then
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("쿠폰 서비스 조회에서 예외 발생시 translator를 호출하여 반환된 예외를 던진다")
    void calculate_fallback_delegate_to_translator() throws Throwable {
        //given
        CouponCommand.Calculate command = fixtureMonkey.giveMeOne(CouponCommand.Calculate.class);
        //발생한 예외
        RuntimeException feignException = new RuntimeException("feignClient 예외");
        //변환된 예외
        ExternalSystemUnavailableException translatedException =
                new ExternalSystemUnavailableException("CODE", "변환된 에러", feignException);
        // feignClient 가 예외를 던짐
        willThrow(feignException).given(client).calculate(any(CouponClientRequest.Calculate.class));
        // translator가 예외를 변환
        given(translator.translate(anyString(), any(Throwable.class)))
                .willReturn(translatedException);
        //when
        //then
        assertThatThrownBy(() -> couponAdaptor.calculate(command))
                .isInstanceOf(ExternalSystemUnavailableException.class);
    }
}

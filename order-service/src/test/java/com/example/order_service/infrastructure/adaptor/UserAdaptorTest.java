package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.UserFeignClient;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static com.example.order_service.support.TestFixtureUtil.giveMeOne;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@IsolatedTest
public class UserAdaptorTest {
    @Autowired
    private UserAdaptor userAdaptor;
    @MockitoBean
    private UserFeignClient client;
    @MockitoBean
    private ExternalExceptionTranslator translator;

    @Test
    @DisplayName("유저 프로필을 조회한다")
    void getUserProfile() {
        //given
        Long userId = 1L;
        UserClientResponse.Profile mockResponse = giveMeOne(UserClientResponse.Profile.class);
        given(client.getUserProfile(anyLong()))
                .willReturn(mockResponse);
        //when
        UserClientResponse.Profile response = userAdaptor.getUserProfile(userId);
        //then
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("유저 프로필 조회중 예외 발생시 translator를 호출하여 변환된 예외가 발생한다")
    void getUserProfile_fallback_delegate_to_translator() throws Throwable {
        //given
        Long userId = 1L;
        RuntimeException feignException = new RuntimeException("feignClient 예외");
        ExternalSystemUnavailableException translatedException =
                new ExternalSystemUnavailableException("CODE", "변환된 에러", feignException);
        given(client.getUserProfile(any())).willThrow(feignException);
        given(translator.translate(anyString(), any(Throwable.class)))
                .willReturn(translatedException);
        //when
        //then
        assertThatThrownBy(() -> userAdaptor.getUserProfile(userId))
                .isInstanceOf(ExternalSystemUnavailableException.class);
    }

    @Test
    @DisplayName("유저 포인트 잔액을 조회한다")
    void getUserPoints(){
        //given
        Long userId = 1L;
        UserClientResponse.UserPoints mockResponse = fixtureMonkey.giveMeOne(UserClientResponse.UserPoints.class);
        given(client.getUserPoints(any())).willReturn(mockResponse);
        //when
        UserClientResponse.UserPoints response = userAdaptor.getUserPoints(userId);
        //then
        assertThat(response).usingRecursiveComparison()
                .isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("유저 포인트 잔액 조회중 예외 발생시 translator를 호출하여 변환된 예외가 발생한다")
    void getUserPoints_fallback_delegate_to_translator() throws Throwable {
        //given
        Long userId = 1L;
        RuntimeException feignException = new RuntimeException("feignClient 예외");
        ExternalSystemUnavailableException translatedException =
                new ExternalSystemUnavailableException("CODE", "변환된 에러", feignException);
        given(client.getUserPoints(any())).willThrow(feignException);
        given(translator.translate(anyString(), any(Throwable.class)))
                .willReturn(translatedException);
        //when
        //then
        assertThatThrownBy(() -> userAdaptor.getUserPoints(userId))
                .isInstanceOf(ExternalSystemUnavailableException.class);
    }

    @Test
    @DisplayName("유저 포인트 사용 검증 정보를 조회한다")
    void getUserPointsForOrder() {
        //given
        Long userId = 1L;
        Long usedPoints = 1000L;
        UserClientResponse.UserPoints mockResponse = fixtureMonkey.giveMeOne(UserClientResponse.UserPoints.class);
        given(client.getUserPointsForOrder(anyLong(), any())).willReturn(mockResponse);
        //when
        UserClientResponse.UserPoints response = userAdaptor.getUserPointsForOrder(userId, usedPoints);
        //then
        assertThat(response).usingRecursiveComparison().isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("유저 포인트 사용 검증 정보 조회중 예외 발생시 translator를 호출하여 변환된 예외가 발생한다")
    void getUserPointsForOrder_fallback_delegate_to_translator() throws Throwable {
        //given
        Long userId = 1L;
        Long usedPoints = 1000L;
        RuntimeException feignException = new RuntimeException("feignClient 예외");
        ExternalSystemUnavailableException translatedException =
                new ExternalSystemUnavailableException("CODE", "변환된 에러", feignException);
        given(client.getUserPointsForOrder(anyLong(), any())).willThrow(feignException);
        given(translator.translate(anyString(), any())).willReturn(translatedException);
        //when
        //then
        assertThatThrownBy(() -> userAdaptor.getUserPointsForOrder(userId, usedPoints))
                .isInstanceOf(ExternalSystemUnavailableException.class);
    }
}

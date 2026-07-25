package com.example.order_service.infrastructure.gateway;

import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.UserFeignClient;
import com.example.order_service.infrastructure.dto.response.user.UserPointsResponse;
import com.example.order_service.infrastructure.dto.response.user.UserProfileResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@IsolatedTest
public class UserGatewayTest {
    @Autowired
    private UserGateway userGateway;
    @MockitoBean
    private UserFeignClient client;
    @MockitoBean
    private ExternalExceptionTranslator translator;

    @Test
    @DisplayName("유저 프로필을 조회한다")
    void getUserProfile() {
        //given
        Long userId = 1L;
        UserProfileResponse mockResponse = Instancio.create(UserProfileResponse.class);
        given(client.getUserProfile(anyLong())).willReturn(mockResponse);
        //when
        UserProfileResponse response = userGateway.getUserProfile(userId);
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
        given(client.getUserProfile(anyLong())).willThrow(feignException);
        given(translator.translate(anyString(), any(Throwable.class))).willReturn(translatedException);
        //when
        //then
        assertThatThrownBy(() -> userGateway.getUserProfile(userId))
                .isInstanceOf(ExternalSystemUnavailableException.class);
    }

    @Test
    @DisplayName("유저 포인트 잔액을 조회한다")
    void getUserPoints(){
        //given
        Long userId = 1L;
        UserPointsResponse mockResponse = Instancio.create(UserPointsResponse.class);
        given(client.getUserPoints(anyLong())).willReturn(mockResponse);
        //when
        UserPointsResponse response = userGateway.getUserPoints(userId);
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
        assertThatThrownBy(() -> userGateway.getUserPoints(userId))
                .isInstanceOf(ExternalSystemUnavailableException.class);
    }
}

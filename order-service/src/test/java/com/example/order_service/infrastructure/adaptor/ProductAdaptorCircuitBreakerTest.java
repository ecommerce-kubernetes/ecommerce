package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.ProductFeignClient;
import com.example.order_service.infrastructure.dto.command.ProductCommand;
import com.example.order_service.infrastructure.dto.request.ProductClientRequest;
import com.example.order_service.support.annotation.IsolatedTest;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@IsolatedTest
@TestPropertySource(properties = {
        "resilience4j.circuitbreaker.instances.productService.sliding-window-size=3",
        "resilience4j.circuitbreaker.instances.productService.minimum-number-of-calls=3",
        "resilience4j.circuitbreaker.instances.productService.failure-rate-threshold=100",
        // 서킷 브레이커 카운트 제외
        "resilience4j.circuitbreaker.instances.productService.ignore-exceptions[0]=com.example.order_service.common.exception.external.ExternalClientException"
})
public class ProductAdaptorCircuitBreakerTest {

    @Autowired
    private ProductAdaptor adaptor;
    @MockitoBean
    private ProductFeignClient client;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        circuitBreakerRegistry.circuitBreaker("productService").reset();
    }

    @Test
    @DisplayName("상품 서비스에서 연속으로 서버 에러가 발생한 경우 서킷브레이커가 열려 요청이 차단된다")
    void circuitbreaker_opens_after_consecutive_server_failures() {
        //given
        ProductCommand.Validate command = fixtureMonkey.giveMeOne(ProductCommand.Validate.class);
        // 타임 아웃 에러가 발생한다고 가정
        given(client.getProductsForOrder(any()))
                .willThrow(new RuntimeException("Connection Timeout"));
        //when
        //then
        // 상품 서비스 에러
        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> adaptor.getProductsForOrder(command))
                    .isInstanceOf(ExternalSystemUnavailableException.class)
                    .hasMessage("PRODUCT-SERVICE 통신 장애");
        }

        //서킷브레이커 open
        assertThatThrownBy(() -> adaptor.getProductsForOrder(command))
                .isInstanceOf(ExternalSystemUnavailableException.class)
                .hasMessage("PRODUCT-SERVICE 서킷 브레이커 열림")
                .extracting("errorCode")
                .isEqualTo("CIRCUIT_BREAKER_OPEN");

        //서킷브레이커가 열렸으므로 클라이언트는 4번의 요청중 3번만 호출됨
        verify(client, times(3)).getProductsForOrder(any());
    }
    
    @Test
    @DisplayName("상품 서비스에서 연속으로 클라이언트 에러가 발생한 경우 서킷브레이커는 닫혀있어야 한다")
    void circuitbreaker_close_after_consecutive_client_failures() {
        //given
        ProductCommand.Validate command = fixtureMonkey.giveMeOne(ProductCommand.Validate.class);
        given(client.getProductsForOrder(any()))
                .willThrow(new ExternalClientException("NOT_PERMISSION", "조회할 권한이 없습니다"));
        //when
        //then
        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> adaptor.getProductsForOrder(command))
                    .isInstanceOf(ExternalClientException.class);
        }
        assertThatThrownBy(() -> adaptor.getProductsForOrder(command))
                .isInstanceOf(ExternalClientException.class);

        //반복된 에러가 클라이언트 예외이므로 정상 요청이 실행되어 4번 호출됨
        verify(client, times(4)).getProductsForOrder(any());
    }
}

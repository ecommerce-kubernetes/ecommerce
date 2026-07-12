package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.gateway.DefaultGatewayException;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.ProductAdaptor;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.mapper.OrderProductMapper;
import com.example.order_service.order.exception.OrderErrorCode;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
public class OrderProductGatewayTest {

    @InjectMocks
    private OrderProductGateway orderProductGateway;
    @Mock
    private ProductAdaptor adaptor;
    @Mock
    private OrderProductMapper productMapper;

    @Nested
    @DisplayName("상품 조회")
    class GetProducts {

        @Test
        @DisplayName("상품을 조회한다")
        void getProducts(){
            //given
            List<Long> variantIds = List.of(1L, 2L);
            ProductClientResponse.ProductList productResponse = Instancio.create(ProductClientResponse.ProductList.class);
            OrderProductResult.ProductList productList = Instancio.create(OrderProductResult.ProductList.class);
            given(adaptor.getProducts(any())).willReturn(productResponse);
            given(productMapper.toResult(any(ProductClientResponse.ProductList.class))).willReturn(productList);
            //when
            OrderProductResult.ProductList result = orderProductGateway.getProducts(variantIds);
            //then
            assertThat(result).isEqualTo(productList);
        }

        @Test
        @DisplayName("상품 조회중 상품 서비스에서 서버 오류 발생시 예외로 변환된다")
        void getProducts_ExternalServerException(){
            //given
            String code = "INTERNAL_SERVER_ERROR";
            String message = "알 수 없는 에러가 발생했습니다";
            List<Long> variantIds = List.of(1L, 2L);
            willThrow(new ExternalServerException(code, message))
                    .given(adaptor).getProducts(any());
            //when
            //then
            assertThatThrownBy(() -> orderProductGateway.getProducts(variantIds))
                    .isInstanceOf(DefaultGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(OrderErrorCode.ORDER_PRODUCT_SERVER_ERROR, code);
        }

        @Test
        @DisplayName("상품 조회중 상품 서비스에서 클라이언트 오류 발생시 예외로 변환된다")
        void getProducts_ExternalClientException(){
            //given
            String code = "INVALID_PRODUCT_REQUEST";
            String message = "잘못된 상품 조회 요청입니다";
            List<Long> variantIds = List.of(1L, 2L);
            willThrow(new ExternalClientException(code, message))
                    .given(adaptor).getProducts(any());
            //when
            //then
            assertThatThrownBy(() -> orderProductGateway.getProducts(variantIds))
                    .isInstanceOf(DefaultGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(OrderErrorCode.ORDER_PRODUCT_CLIENT_ERROR, code);
        }

        @Test
        @DisplayName("상품 조회중 상품 서비스에서 가용 불가 오류 발생시 예외로 변환된다")
        void getProducts_ExternalSystemUnavailableException(){
            //given
            String code = "SERVICE_UNAVAILABLE";
            String message = "상품 서비스 통신 장애";
            List<Long> variantIds = List.of(1L, 2L);
            willThrow(new ExternalSystemUnavailableException(code, message))
                    .given(adaptor).getProducts(any());
            //when
            //then
            assertThatThrownBy(() -> orderProductGateway.getProducts(variantIds))
                    .isInstanceOf(DefaultGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(OrderErrorCode.ORDER_PRODUCT_UNAVAILABLE_SERVER_ERROR, code);
        }

        @Test
        @DisplayName("상품 조회중 상품 서비스 서킷 브레이커가 열린 경우 예외가 발생한다")
        void getProducts_ExternalCircuitBreakerException(){
            //given
            String code = "PRODUCT_CIRCUIT_OPEN";
            String message = "상품 서비스 서킷 오픈";
            List<Long> variantIds = List.of(1L, 2L);
            willThrow(new ExternalCircuitBreakerException(code, message))
                    .given(adaptor).getProducts(any());
            //when
            //then
            assertThatThrownBy(() -> orderProductGateway.getProducts(variantIds))
                    .isInstanceOf(DefaultGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(OrderErrorCode.ORDER_PRODUCT_CIRCUIT_OPEN, code);
        }
    }
}

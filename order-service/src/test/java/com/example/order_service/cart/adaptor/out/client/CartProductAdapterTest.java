package com.example.order_service.cart.adaptor.out.client;

import com.example.order_service.cart.adaptor.out.client.mapper.CartProductAdapterMapper;
import com.example.order_service.cart.application.fixture.CartProductFixture;
import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.cart.application.port.dto.CartProductStatus;
import com.example.order_service.cart.exception.CartProductPortErrorCode;
import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.PortException;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
import com.example.order_service.infrastructure.fixture.ProductResponseFixture;
import com.example.order_service.infrastructure.gateway.ProductGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class CartProductAdapterTest {

    @InjectMocks
    private CartProductAdapter cartProductAdapter;
    @Mock
    private ProductGateway productGateway;
    @Spy
    private CartProductAdapterMapper cartProductAdapterMapper;

    @BeforeEach
    void setUp() {
        CartProductAdapterMapper mapper = new CartProductAdapterMapper();
        cartProductAdapter = new CartProductAdapter(productGateway, mapper);
    }

    @Test
    @DisplayName("상품을 조회한다")
    void getProducts() {
        //given
        List<Long> productVariantIds = List.of(1L);
        ProductResponse response = ProductResponseFixture.anProductResponse().build();

        given(productGateway.getProducts(anyList())).willReturn(response);
        //when
        CartProductResult result = cartProductAdapter.getProducts(productVariantIds);
        //then
        assertThat(result.products()).hasSize(1);
        assertThat(result.products())
                .extracting("productVariantId")
                .containsExactly(1L);
    }


    @Test
    @DisplayName("상품 조회중 상품 서비스에서 서버 오류 발생시 예외로 변환된다")
    void getProducts_ExternalServerException() {
        //given
        String code = "INTERNAL_SERVER_ERROR";
        String message = "알 수 없는 에러가 발생했습니다";
        List<Long> variantIds = List.of(1L, 2L);
        willThrow(new ExternalServerException(code, message))
                .given(productGateway).getProducts(anyList());
        //when
        //then
        assertThatThrownBy(() -> cartProductAdapter.getProducts(variantIds))
                .isInstanceOf(PortException.class)
                .hasMessage(String.format("Port Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CartProductPortErrorCode.PRODUCT_SERVER_ERROR, code);
    }


    @Test
    @DisplayName("상품 조회중 상품 서비스에서 클라이언트 오류 발생시 예외로 변환된다")
    void getProducts_ExternalClientException() {
        //given
        String code = "INVALID_PRODUCT_REQUEST";
        String message = "잘못된 상품 조회 요청입니다";
        List<Long> variantIds = List.of(1L, 2L);
        willThrow(new ExternalClientException(code, message))
                .given(productGateway).getProducts(anyList());
        //when
        //then
        assertThatThrownBy(() -> cartProductAdapter.getProducts(variantIds))
                .isInstanceOf(PortException.class)
                .hasMessage(String.format("Port Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CartProductPortErrorCode.PRODUCT_CLIENT_ERROR, code);
    }

    @Test
    @DisplayName("상품 조회중 상품 서비스에서 가용 불가 오류 발생시 예외로 변환된다")
    void getProducts_ExternalSystemUnavailableException() {
        //given
        String code = "SERVICE_UNAVAILABLE";
        String message = "상품 서비스 통신 장애";
        List<Long> variantIds = List.of(1L, 2L);
        willThrow(new ExternalSystemUnavailableException(code, message))
                .given(productGateway).getProducts(anyList());
        //when
        //then
        assertThatThrownBy(() -> cartProductAdapter.getProducts(variantIds))
                .isInstanceOf(PortException.class)
                .hasMessage(String.format("Port Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CartProductPortErrorCode.PRODUCT_UNAVAILABLE_SERVER_ERROR, code);
    }

    @Test
    @DisplayName("상품 조회중 상품 서비스 서킷 브레이커가 열린 경우 예외가 발생한다")
    void getProducts_ExternalCircuitBreakerException(){
        //given
        String code = "PRODUCT_CIRCUIT_OPEN";
        String message = "상품 서비스 서킷 오픈";
        List<Long> variantIds = List.of(1L, 2L);
        willThrow(new ExternalCircuitBreakerException(code, message))
                .given(productGateway).getProducts(anyList());
        //when
        //then
        assertThatThrownBy(() -> cartProductAdapter.getProducts(variantIds))
                .isInstanceOf(PortException.class)
                .hasMessage(String.format("Port Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(CartProductPortErrorCode.PRODUCT_CIRCUIT_OPEN, code);
    }
}
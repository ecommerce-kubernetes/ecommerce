package com.example.order_service.cart.infrastructure.adaptor;

import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.cart.application.port.dto.CartProductStatus;
import com.example.order_service.cart.infrastructure.adaptor.mapper.CartProductPortMapper;
import com.example.order_service.cart.infrastructure.adaptor.mapper.CartProductPortMapperImpl;
import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.common.exception.gateway.DefaultPortException;
import com.example.order_service.common.exception.gateway.ProductGatewayErrorCode;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.common.mapper.MoneyMapperImpl;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
import com.example.order_service.infrastructure.gateway.ProductGateway;
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
class CartProductAdaptorTest {

    @InjectMocks
    private CartProductAdaptor cartProductAdaptor;
    @Mock
    private ProductGateway productGateway;
    @Spy
    private MoneyMapper moneyMapper = new MoneyMapperImpl();
    @Spy
    private CartProductPortMapper cartProductPortMapper = new CartProductPortMapperImpl(moneyMapper);

    @Test
    @DisplayName("상품을 조회한다")
    void getProducts() {
        //given
        List<Long> variantIds = List.of(1L, 2L);
        ProductResponse.UnitPrice unitPrice = ProductResponse.UnitPrice.builder()
                .originalPrice(10000L)
                .discountRate(10)
                .discountAmount(1000L)
                .discountedPrice(9000L)
                .build();

        ProductResponse.ProductOption option = ProductResponse.ProductOption.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL").build();
        ProductResponse.ProductDetail detail = ProductResponse.ProductDetail.builder()
                .productId(1L)
                .productVariantId(1L)
                .status("ON_SALE")
                .stock(100)
                .sku("SKU")
                .productName("상품 이름")
                .thumbnail("/product/product.jpg")
                .unitPrice(unitPrice)
                .options(List.of(option))
                .build();
        ProductResponse response = ProductResponse.builder().products(List.of(detail)).build();
        given(productGateway.getProducts(anyList())).willReturn(response);
        //when
        CartProductResult result = cartProductAdaptor.getProducts(variantIds);
        //then
        assertThat(result.products()).hasSize(1);
        CartProductResult.CartProductDetail mappedProduct = result.products().stream().findFirst().orElseThrow();
        assertThat(mappedProduct.status()).isEqualTo(CartProductStatus.ON_SALE);
        assertThat(mappedProduct.productId()).isEqualTo(1L);
        assertThat(mappedProduct.discountedPrice()).isEqualTo(Money.wons(9000L));
        assertThat(mappedProduct.stock()).isEqualTo(100);
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
        assertThatThrownBy(() -> cartProductAdaptor.getProducts(variantIds))
                .isInstanceOf(DefaultPortException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(ProductGatewayErrorCode.PRODUCT_SERVER_ERROR, code);
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
        assertThatThrownBy(() -> cartProductAdaptor.getProducts(variantIds))
                .isInstanceOf(DefaultPortException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(ProductGatewayErrorCode.PRODUCT_CLIENT_ERROR, code);
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
        assertThatThrownBy(() -> cartProductAdaptor.getProducts(variantIds))
                .isInstanceOf(DefaultPortException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(ProductGatewayErrorCode.PRODUCT_UNAVAILABLE_SERVER_ERROR, code);
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
        assertThatThrownBy(() -> cartProductAdaptor.getProducts(variantIds))
                .isInstanceOf(DefaultPortException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(ProductGatewayErrorCode.PRODUCT_CIRCUIT_OPEN, code);
    }
}
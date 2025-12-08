package com.example.order_service.api.cart.infrastructure.client;

import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import com.example.order_service.api.common.exception.server.InternalServerException;
import com.example.order_service.api.common.exception.server.UnavailableServiceException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CartProductClientServiceTest {

    @InjectMocks
    private CartProductClientService cartProductClientService;

    @Mock
    private CartProductClient cartProductClient;

    @Test
    @DisplayName("상품 서비스에서 상품의 정보를 조회한다")
    void getProduct(){
        //given
        CartProductResponse product = createProductResponse(1L, 1L, "상품1", 3000L, 10,
                "http://thumbnail.jpg", List.of(CartProductResponse.ItemOption.builder().optionTypeName("사이즈").optionValueName("XL").build()));

        given(cartProductClient.getProductByVariantId(anyLong()))
                .willReturn(product);
        //when
        CartProductResponse response = cartProductClientService.getProduct(1L);
        //then
        assertThat(response)
                .extracting("productId", "productVariantId", "productName", "thumbnailUrl")
                .contains(1L, 1L, "상품1", "http://thumbnail.jpg");

        assertThat(response.getUnitPrice())
                .extracting("originalPrice", "discountRate", "discountAmount", "discountedPrice")
                .contains(3000L, 10, 300L, 2700L);
    }

    @Test
    @DisplayName("서킷 브레이커가 열리면 503 예외를 던진다")
    void getProductWhenOpenCircuitBreaker(){
        //given
        CallNotPermittedException exception = CallNotPermittedException
                .createCallNotPermittedException(CircuitBreaker.ofDefaults("productService"));
        //when
        //then
        assertThatThrownBy(() ->
                ReflectionTestUtils.invokeMethod(cartProductClientService, "getProductFallback", 1L, exception)
        )
                .isInstanceOf(UnavailableServiceException.class)
                .hasMessage("상품 서비스가 응답하지 않습니다");
    }

    @Test
    @DisplayName("500 응답이 오면 InternalServerError를 던진다")
    void getProductWhen500Code(){
        //given
        InternalServerException exception = new InternalServerException("상품 서비스 장애 발생");
        //when
        //then
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(cartProductClientService, "getProductFallback", 1L, exception))
                .isInstanceOf(InternalServerException.class)
                .hasMessage("상품 서비스 장애 발생");
    }

    @Test
    @DisplayName("상품 서비스에서 상품들의 정보를 조회한다")
    void getProducts(){
        //given
        CartProductResponse product1 = createProductResponse(1L, 1L, "상품1", 3000L, 10,
                "http://thumbnail1.jpg", List.of(CartProductResponse.ItemOption.builder().optionTypeName("사이즈").optionValueName("XL").build()));

        CartProductResponse product2 = createProductResponse(2L, 2L, "상품2", 5000L, 10,
                "http://thumbnail2.jpg", List.of(CartProductResponse.ItemOption.builder().optionTypeName("용량").optionValueName("256").build()));

        given(cartProductClient.getProductVariantByIds(anyList()))
                .willReturn(List.of(product1, product2));
        //when
        List<CartProductResponse> products = cartProductClientService.getProducts(List.of(1L, 2L));
        //then
        assertThat(products)
                .hasSize(2) // 1. 리스트 크기 확인
                .extracting("productId", "productVariantId", "productName") // 2. 검증할 필드 추출
                .containsExactly( // 3. 순서와 값 검증
                        tuple(1L, 1L, "상품1"),
                        tuple(2L, 2L, "상품2")
                );
    }

    @Test
    @DisplayName("상품 목록을 조회할때 서킷 브레이커가 열리면 빈 배열을 반환한다")
    void getProductsWhenOpenCircuitBreaker(){
        //given
        CallNotPermittedException exception = CallNotPermittedException
                .createCallNotPermittedException(CircuitBreaker.ofDefaults("productService"));
        //when
        List<CartProductResponse> result = ReflectionTestUtils.invokeMethod(
                cartProductClientService,
                "getProductsFallback", // 메서드 이름 정확해야 함
                List.of(1L, 2L),
                exception
        );
        //then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("상품 목록을 조회할때 알 수 없는 에러가 발생하면 빈 배열을 반환한다")
    void getProductsWhenOtherException(){
        //given
        InternalServerException exception = new InternalServerException("상품 서비스 장애 발생");
        //when
        List<CartProductResponse> result = ReflectionTestUtils.invokeMethod(
                cartProductClientService,
                "getProductsFallback", // 메서드 이름 정확해야 함
                List.of(1L, 2L),
                exception
        );
        //then
        assertThat(result).isEmpty();
    }

    private CartProductResponse createProductResponse(Long productId, Long productVariantId,
                                                      String productName, Long originalPrice, int discountRate,
                                                      String thumbnail, List<CartProductResponse.ItemOption> options){
        long discountAmount = originalPrice * discountRate / 100;
        return CartProductResponse.builder()
                .productId(productId)
                .productVariantId(productVariantId)
                .productName(productName)
                .unitPrice(
                        CartProductResponse.UnitPrice.builder()
                                .originalPrice(originalPrice)
                                .discountRate(discountRate)
                                .discountAmount(discountAmount)
                                .discountedPrice(originalPrice - discountAmount)
                                .build())
                .thumbnailUrl(thumbnail)
                .itemOptions(options)
                .build();
    }
}

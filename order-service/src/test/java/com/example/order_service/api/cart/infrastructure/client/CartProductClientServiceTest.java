package com.example.order_service.api.cart.infrastructure.client;

import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.server.InternalServerException;
import com.example.order_service.api.common.exception.server.UnavailableServiceException;
import com.example.order_service.api.support.ExcludeInfraServiceTest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

public class CartProductClientServiceTest extends ExcludeInfraServiceTest {

    @Autowired
    private CartProductClientService cartProductClientService;

    @MockitoBean
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
    @DisplayName("서킷브레이커가 열렸을때 상품을 조회하면 UnavailableService 예외를 던진다")
    void getProduct_When_Open_CircuitBreaker(){
        //given
        willThrow(CallNotPermittedException.class).given(cartProductClient).getProductByVariantId(anyLong());
        //when
        //then
        assertThatThrownBy(() -> cartProductClientService.getProduct(1L))
                .isInstanceOf(UnavailableServiceException.class)
                .hasMessage("상품 서비스가 응답하지 않습니다");
    }

    @Test
    @DisplayName("상품을 조회할때 알 수 없는 에러가 발생한 경우 InternalServerException을 던진다")
    void getProduct_When_Unknown_Exception(){
        //given
        willThrow(new InternalServerException("상품 서비스 장애 발생")).given(cartProductClient)
                        .getProductByVariantId(anyLong());
        //when
        //then
        assertThatThrownBy(() -> cartProductClientService.getProduct(1L))
                .isInstanceOf(InternalServerException.class)
                .hasMessage("상품 서비스 장애 발생");
    }
    
    @Test
    @DisplayName("상품을 조회할때 상품을 찾을 수 없는 경우 받은 예외를 그대로 던진다")
    void getProduct_When_NotFound_Exception() {
        //given
        willThrow(new NotFoundException("상품을 찾을 수 없습니다"))
                .given(cartProductClient)
                .getProductByVariantId(anyLong());
        //when
        //then
        assertThatThrownBy(() -> cartProductClientService.getProduct(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("상품을 찾을 수 없습니다");
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
    void getProducts_When_Open_CircuitBreaker(){
        //given
        willThrow(CallNotPermittedException.class)
                .given(cartProductClient)
                .getProductVariantByIds(anyList());
        //when
        List<CartProductResponse> products = cartProductClientService.getProducts(List.of(1L, 2L));
        //then
        assertThat(products).isEmpty();
    }

    @Test
    @DisplayName("상품 목록을 조회할때 알 수 없는 에러가 발생하면 빈 배열을 반환한다")
    void getProducts_When_Other_Exception(){
        //given
        willThrow(InternalServerException.class)
                .given(cartProductClient)
                .getProductVariantByIds(anyList());
        //when
        List<CartProductResponse> products = cartProductClientService.getProducts(List.of(1L, 2L));
        //then
        assertThat(products).isEmpty();
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

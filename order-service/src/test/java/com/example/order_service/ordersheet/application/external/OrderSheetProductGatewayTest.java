package com.example.order_service.ordersheet.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.business.code.OrderSheetErrorCode;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.ProductAdaptor;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import com.example.order_service.ordersheet.application.mapper.OrderSheetProductMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
public class OrderSheetProductGatewayTest {

    @InjectMocks
    private OrderSheetProductGateway orderSheetProductGateway;
    @Mock
    private ProductAdaptor adaptor;
    @Spy
    private OrderSheetProductMapper productMapper = Mappers.getMapper(OrderSheetProductMapper.class);

    @Nested
    @DisplayName("상품 조회")
    class GetProducts {

        @Test
        @DisplayName("주문 상품 정보를 조회한다")
        void getProducts(){
            //given
            List<Long> variantIds = List.of(1L, 2L);
            List<ProductClientResponse.Product> productResponses = variantIds.stream()
                    .map(id -> fixtureMonkey.giveMeBuilder(ProductClientResponse.Product.class)
                            .set("productVariantId", id)
                            .sample()).toList();
            given(adaptor.getProductsByVariantIds(anyList()))
                    .willReturn(productResponses);
            //when
            List<OrderSheetProductResult.Info> products = orderSheetProductGateway.getProducts(variantIds);
            //then
            assertThat(products)
                    .extracting("productVariantId")
                    .containsExactlyInAnyOrder(
                            1L, 2L
                    );
        }

        @Test
        @DisplayName("상품 조회중 상품 서비스에서 서버 오류가 발생한 경우 비지니스 예외로 변경하여 던진다")
        void getProducts_ExternalServerException(){
            //given
            List<Long> variantIds = List.of(1L, 2L);
            willThrow(new ExternalServerException("상품 서비스 에러 발생"))
                    .given(adaptor).getProductsByVariantIds(anyList());
            //when
            //then
            assertThatThrownBy(() -> orderSheetProductGateway.getProducts(variantIds))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_SERVER_ERROR);
        }

        @Test
        @DisplayName("상품 조회중 상품 서비스에서 클라이언트 오류가 발생한 경우 비지니스 예외로 변경하여 던진다")
        void getProducts_ExternalClientException() {
            //given
            List<Long> variantIds = List.of(1L, 2L);
            willThrow(new ExternalClientException("상품 서비스 에러 발생"))
                    .given(adaptor).getProductsByVariantIds(anyList());
            //when
            //then
            assertThatThrownBy(() -> orderSheetProductGateway.getProducts(variantIds))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_CLIENT_ERROR);
        }

        @Test
        @DisplayName("상품 조회중 상품 서비스에서 사용 불가 오류가 발생한 경우 비지니스 예외로 변경하여 던진다")
        void getProducts_ExternalUnavailableException(){
            //given
            List<Long> variantIds = List.of(1L, 2L);
            willThrow(new ExternalSystemUnavailableException("상품 서비스 에러 발생"))
                    .given(adaptor).getProductsByVariantIds(anyList());
            //when
            //then
            assertThatThrownBy(() -> orderSheetProductGateway.getProducts(variantIds))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_UNAVAILABLE_SERVER_ERROR);
        }
    }
}

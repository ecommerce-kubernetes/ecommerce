package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.ProductAdaptor;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.order.application.dto.result.OrderProductResult;
import com.example.order_service.order.application.mapper.OrderProductMapper;
import com.example.order_service.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
public class OrderProductDeprecatedGatewayTest {

    @InjectMocks
    private OrderProductGateway orderProductGateway;

    @Mock
    private ProductAdaptor adaptor;
    @Mock
    private OrderProductMapper productMapper;

    @Nested
    @DisplayName("상품 정보 조회")
    class GetProducts {

        @Test
        @DisplayName("주문 상품 정보를 조회한다")
        void getProducts() {
            //given
            List<Long> variantIds = List.of(1L, 2L);
            List<ProductClientResponse.ProductDeprecated> productDeprecatedRespons = fixtureMonkey.giveMe(ProductClientResponse.ProductDeprecated.class, 2);
            List<OrderProductResult.Info> mockInfos = fixtureMonkey.giveMe(OrderProductResult.Info.class, 2);
            given(adaptor.getProductsByVariantIds(anyList())).willReturn(productDeprecatedRespons);
            given(productMapper.toResult(any())).willReturn(mockInfos.get(0), mockInfos.get(1));
            //when
            List<OrderProductResult.Info> result = orderProductGateway.getProducts(variantIds);
            //then
            assertThat(result).containsExactlyElementsOf(mockInfos);
        }

        @Test
        @DisplayName("상품 조회중 상품 서비스에서 서버 오류가 발생한 경우 비지니스 예외로 변경하여 던진다")
        void getProducts_ExternalServerException() {
            //given
            List<Long> variantIds = List.of(1L, 2L);
            willThrow(new ExternalServerException("INTERNAL_SERVER_ERROR", "처리중 오류가 발생했습니다"))
                    .given(adaptor).getProductsByVariantIds(anyList());
            //when
            //then
            assertThatThrownBy(() -> orderProductGateway.getProducts(variantIds))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_PRODUCT_SERVER_ERROR);
        }

        @Test
        @DisplayName("상품 조회중 상품 서비스에서 클라이언트 오류가 발생한 경우 비지니스 예외로 변경하여 던진다")
        void getProducts_ExternalClientException(){
            //given
            List<Long> variantIds = List.of(1L, 2L);
            willThrow(new ExternalClientException("NOT_PERMISSION", "조회 권한이 없습니다"))
                    .given(adaptor).getProductsByVariantIds(anyList());
            //when
            //then
            assertThatThrownBy(() -> orderProductGateway.getProducts(variantIds))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_PRODUCT_CLIENT_ERROR);
        }

        @Test
        @DisplayName("상품 조회중 상품 서비스에서 사용 불가 오류가 발생한 경우 비지니스 예외로 변경하여 던진다")
        void getProducts_ExternalUnavailableException() {
            //given
            List<Long> variantIds = List.of(1L, 2L);
            willThrow(new ExternalSystemUnavailableException("SERVICE_UNAVAILABLE", "상품 서비스 통신 오류"))
                    .given(adaptor).getProductsByVariantIds(anyList());
            //when
            //then
            assertThatThrownBy(() -> orderProductGateway.getProducts(variantIds))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_PRODUCT_UNAVAILABLE_SERVER_ERROR);
        }
    }
}

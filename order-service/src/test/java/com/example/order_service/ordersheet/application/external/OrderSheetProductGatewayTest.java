package com.example.order_service.ordersheet.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.ProductAdaptor;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import com.example.order_service.ordersheet.application.mapper.OrderSheetProductMapper;
import com.example.order_service.ordersheet.exception.OrderSheetErrorCode;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
public class OrderSheetProductGatewayTest {

    @InjectMocks
    private OrderSheetProductGateway orderSheetProductGateway;
    @Mock
    private ProductAdaptor adaptor;
    @Mock
    private OrderSheetProductMapper productMapper;

    @Nested
    @DisplayName("상품 조회")
    class GetProducts {

        @Test
        @DisplayName("주문 상품 정보를 조회한다")
        void getProducts(){
            //given
            OrderSheetCommand.OrderItem item1 = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(1L)
                    .quantity(1)
                    .build();
            OrderSheetCommand.OrderItem item2 = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(2L)
                    .quantity(1)
                    .build();
            List<ProductClientResponse.Product> productResponses = fixtureMonkey.giveMe(ProductClientResponse.Product.class, 2);
            List<OrderSheetProductResult.Info> mockInfo = fixtureMonkey.giveMe(OrderSheetProductResult.Info.class, 2);
            given(adaptor.getProductsForOrder(any())).willReturn(productResponses);
            given(productMapper.toResult(any(ProductClientResponse.Product.class))).willReturn(mockInfo.get(0), mockInfo.get(1));
            //when
            List<OrderSheetProductResult.Info> result = orderSheetProductGateway.getProducts(List.of(item1, item2));
            //then
            assertThat(result).containsExactlyElementsOf(mockInfo);
        }

        @Test
        @DisplayName("상품 조회중 상품 서비스에서 서버 오류가 발생한 경우 비지니스 예외로 변경하여 던진다")
        void getProducts_ExternalServerException(){
            //given
            OrderSheetCommand.OrderItem item1 = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(1L)
                    .quantity(1)
                    .build();
            OrderSheetCommand.OrderItem item2 = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(2L)
                    .quantity(1)
                    .build();
            willThrow(new ExternalServerException("INTERNAL_SERVER_ERROR", "처리중 오류가 발생했습니다"))
                    .given(adaptor).getProductsForOrder(any());
            //when
            //then
            assertThatThrownBy(() -> orderSheetProductGateway.getProducts(List.of(item1, item2)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_SERVER_ERROR);
        }

        @Test
        @DisplayName("상품 조회중 상품 서비스에서 클라이언트 오류가 발생한 경우 비지니스 예외로 변경하여 던진다")
        void getProducts_ExternalClientException() {
            //given
            OrderSheetCommand.OrderItem item1 = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(1L)
                    .quantity(1)
                    .build();
            OrderSheetCommand.OrderItem item2 = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(2L)
                    .quantity(1)
                    .build();
            willThrow(new ExternalClientException("NOT_PERMISSION", "조회 권한이 없습니다"))
                    .given(adaptor).getProductsForOrder(any());
            //when
            //then
            assertThatThrownBy(() -> orderSheetProductGateway.getProducts(List.of(item1, item2)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_CLIENT_ERROR);
        }

        @Test
        @DisplayName("상품 조회중 상품 서비스에서 사용 불가 오류가 발생한 경우 비지니스 예외로 변경하여 던진다")
        void getProducts_ExternalUnavailableException(){
            //given
            OrderSheetCommand.OrderItem item1 = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(1L)
                    .quantity(1)
                    .build();
            OrderSheetCommand.OrderItem item2 = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(2L)
                    .quantity(1)
                    .build();
            willThrow(new ExternalSystemUnavailableException("SERVICE_UNAVAILABLE", "상품 서비스 통신 장애"))
                    .given(adaptor).getProductsForOrder(any());
            //when
            //then
            assertThatThrownBy(() -> orderSheetProductGateway.getProducts(List.of(item1, item2)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_UNAVAILABLE_SERVER_ERROR);
        }
    }
}

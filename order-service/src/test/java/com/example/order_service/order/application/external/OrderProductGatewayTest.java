package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.application.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.ProductAdaptor;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.order.application.external.dto.command.OrderProductCommand;
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
            List<OrderProductCommand.OrderItem> orderItems = Instancio.ofList(OrderProductCommand.OrderItem.class)
                    .size(2)
                    .create();
            ProductClientResponse.ProductList productResponse = Instancio.create(ProductClientResponse.ProductList.class);
            OrderProductResult.ProductList productList = Instancio.create(OrderProductResult.ProductList.class);
            given(adaptor.getProducts(any())).willReturn(productResponse);
            given(productMapper.toResult(any(ProductClientResponse.ProductList.class))).willReturn(productList);
            //when
            OrderProductResult.ProductList result = orderProductGateway.getProducts(orderItems);
            //then
            assertThat(result).isEqualTo(productList);
        }

        @Test
        @DisplayName("상품 조회중 상품 서비스에서 서버 오류 발생시 비지니스 예외로 변환된다")
        void getProducts_ExternalServerException(){
            //given
            List<OrderProductCommand.OrderItem> orderItems = Instancio.ofList(OrderProductCommand.OrderItem.class)
                    .size(2)
                    .create();
            willThrow(new ExternalServerException("INTERNAL_SERVER_ERROR", "알 수 없는 에러가 발생했습니다"))
                    .given(adaptor).getProducts(any());
            //when
            //then
            assertThatThrownBy(() -> orderProductGateway.getProducts(orderItems))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_PRODUCT_SERVER_ERROR);
        }

        @Test
        @DisplayName("상품 조회중 상품 서비스에서 클라이언트 오류 발생시 비지니스 예외로 변환된다")
        void getProducts_ExternalClientException(){
            //given
            List<OrderProductCommand.OrderItem> orderItems = Instancio.ofList(OrderProductCommand.OrderItem.class)
                    .size(2)
                    .create();
            willThrow(new ExternalClientException("INVALID_PRODUCT_REQUEST", "잘못된 상품 조회 요청입니다"))
                    .given(adaptor).getProducts(any());
            //when
            //then
            assertThatThrownBy(() -> orderProductGateway.getProducts(orderItems))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_PRODUCT_CLIENT_ERROR);
        }

        @Test
        @DisplayName("상품 조회중 상품 서비스에서 가용 불가 오류 발생시 비지니스 예외로 변환된다")
        void getProducts_ExternalSystemUnavailableException(){
            //given
            List<OrderProductCommand.OrderItem> orderItems = Instancio.ofList(OrderProductCommand.OrderItem.class)
                    .size(2)
                    .create();
            willThrow(new ExternalSystemUnavailableException("SERVICE_UNAVAILABLE", "상품 서비스 통신 장애"))
                    .given(adaptor).getProducts(any());
            //when
            //then
            assertThatThrownBy(() -> orderProductGateway.getProducts(orderItems))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_PRODUCT_UNAVAILABLE_SERVER_ERROR);
        }
    }
}

package com.example.order_service.order.application.external;

import com.example.order_service.api.support.fixture.order.OrderCommandFixture;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.business.code.OrderErrorCode;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.ProductAdaptor;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.order.application.dto.command.CreateOrderItemCommand;
import com.example.order_service.order.application.dto.result.OrderProductResult;
import com.example.order_service.order.application.dto.result.ProductStatus;
import com.example.order_service.order.application.mapper.OrderProductMapper;
import com.example.order_service.order.domain.service.dto.result.OrderProductInfo;
import com.example.order_service.order.infrastructure.client.product.OrderProductAdaptor;
import com.example.order_service.order.infrastructure.client.product.dto.OrderProductResponse;
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

import static com.example.order_service.api.support.fixture.order.OrderProductFixture.anOrderProductInfo;
import static com.example.order_service.api.support.fixture.order.OrderProductFixture.anOrderProductResponse;
import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
public class OrderProductGatewayTest {

    @InjectMocks
    private OrderProductGateway orderProductGateway;

    @Mock
    private OrderProductAdaptor orderProductAdaptor;

    @Mock
    private ProductAdaptor adaptor;
    @Spy
    private OrderProductMapper productMapper = Mappers.getMapper(OrderProductMapper.class);

    @Nested
    @DisplayName("상품 정보 조회")
    class GetProducts {

        @Test
        @DisplayName("주문 상품 정보를 조회한다")
        void getProducts() {
            //given
            List<Long> variantIds = List.of(1L, 2L);
            List<ProductClientResponse.Product> productResponses = variantIds.stream()
                    .map(id -> fixtureMonkey.giveMeBuilder(ProductClientResponse.Product.class)
                            .set("productVariantId", id)
                            .set("status", "ON_SALE")
                            .sample()).toList();
            given(adaptor.getProductsByVariantIds(anyList()))
                    .willReturn(productResponses);
            //when
            List<OrderProductResult.Info> result = orderProductGateway.getProducts(variantIds);
            //then
            assertThat(result)
                    .extracting("productVariantId", "status")
                    .containsExactlyInAnyOrder(
                            tuple(1L, ProductStatus.ORDERABLE),
                            tuple(2L, ProductStatus.ORDERABLE)
                    );
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

        @Test
        @DisplayName("상품 정보를 조회한다")
        void getProductsdeprecated() {
            //given
            CreateOrderItemCommand itemCommand1 = OrderCommandFixture.anOrderItemCommand().productVariantId(1L).quantity(2).build();
            CreateOrderItemCommand itemCommand2 = OrderCommandFixture.anOrderItemCommand().productVariantId(2L).quantity(5).build();
            OrderProductResponse product1 = anOrderProductResponse().productVariantId(1L).status("ON_SALE").build();
            OrderProductResponse product2 = anOrderProductResponse().productVariantId(2L).status("ON_SALE").build();
            OrderProductInfo expectedProductInfo1 = anOrderProductInfo().productVariantId(1L).build();
            OrderProductInfo expectedProductInfo2 = anOrderProductInfo().productVariantId(2L).build();
            given(orderProductAdaptor.getProducts(anyList())).willReturn(List.of(product1, product2));
            //when
            List<OrderProductInfo> result = orderProductGateway.getProductsdeprecated(List.of(itemCommand1, itemCommand2));
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .isEqualTo(List.of(expectedProductInfo1, expectedProductInfo2));
        }

        @Test
        @DisplayName("상품 조회시 없는 상품이 있는 경우 예외를 던진다")
        void getProducts_not_found_product() {
            //given
            CreateOrderItemCommand itemCommand1 = OrderCommandFixture.anOrderItemCommand().productVariantId(1L).quantity(2).build();
            CreateOrderItemCommand itemCommand2 = OrderCommandFixture.anOrderItemCommand().productVariantId(2L).quantity(5).build();
            OrderProductResponse product = anOrderProductResponse().productVariantId(1L).status("ON_SALE").build();
            given(orderProductAdaptor.getProducts(anyList())).willReturn(List.of(product));
            //when
            //then
            assertThatThrownBy(() -> orderProductGateway.getProductsdeprecated(List.of(itemCommand1, itemCommand2)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_PRODUCT_NOT_FOUND);
        }

        @Test
        @DisplayName("상품이 판매중이 아니라면 예외가 발생한다")
        void getProducts_product_not_on_sale() {
            //given
            CreateOrderItemCommand itemCommand1 = OrderCommandFixture.anOrderItemCommand().productVariantId(1L).quantity(2).build();
            CreateOrderItemCommand itemCommand2 = OrderCommandFixture.anOrderItemCommand().productVariantId(2L).quantity(5).build();
            OrderProductResponse product1 = anOrderProductResponse().productVariantId(1L).status("ON_SALE").build();
            // 판매 중지된 상품
            OrderProductResponse product2 = anOrderProductResponse().productVariantId(1L).status("STOP_SALE").build();
            given(orderProductAdaptor.getProducts(anyList()))
                    .willReturn(List.of(product1, product2));
            //when
            //then
            assertThatThrownBy(() -> orderProductGateway.getProductsdeprecated(List.of(itemCommand1, itemCommand2)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_PRODUCT_NOT_ON_SALE);
        }

        @Test
        @DisplayName("상품 수량이 부족하면 예외가 발생한다")
        void getProducts_product_quantity_insufficient() {
            //given
            CreateOrderItemCommand itemCommand1 = OrderCommandFixture.anOrderItemCommand().productVariantId(1L).quantity(2).build();
            CreateOrderItemCommand itemCommand2 = OrderCommandFixture.anOrderItemCommand().productVariantId(2L).quantity(5).build();
            OrderProductResponse product1 = anOrderProductResponse().productVariantId(1L).status("ON_SALE").build();
            // 재고가 부족한 상품
            OrderProductResponse product2 = anOrderProductResponse().productVariantId(2L).status("ON_SALE").stockQuantity(3).build();
            given(orderProductAdaptor.getProducts(anyList()))
                    .willReturn(List.of(product1, product2));
            //when
            //then
            assertThatThrownBy(() -> orderProductGateway.getProductsdeprecated(List.of(itemCommand1, itemCommand2)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_PRODUCT_INSUFFICIENT_STOCK);
        }
    }
}

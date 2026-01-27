package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.domain.service.dto.result.OrderProductInfo;
import com.example.order_service.api.order.facade.dto.command.CreateOrderItemCommand;
import com.example.order_service.api.order.infrastructure.client.product.OrderProductAdaptor;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.domain.model.ProductStatus;
import com.example.order_service.api.support.fixture.OrderCommandFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.example.order_service.api.support.fixture.OrderProductFixture.anOrderProductInfo;
import static com.example.order_service.api.support.fixture.OrderProductFixture.anOrderProductResponse;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderProductServiceTest {

    @InjectMocks
    private OrderProductService orderProductService;

    @Mock
    private OrderProductAdaptor orderProductAdaptor;

    @Nested
    @DisplayName("상품 정보 조회")
    class GetProducts {

        @Test
        @DisplayName("상품 정보를 조회한다")
        void getProducts(){
            //given
            CreateOrderItemCommand itemCommand1 = OrderCommandFixture.anOrderItemCommand().productVariantId(1L).quantity(2).build();
            CreateOrderItemCommand itemCommand2 = OrderCommandFixture.anOrderItemCommand().productVariantId(2L).quantity(5).build();
            OrderProductResponse product1 = anOrderProductResponse().productVariantId(1L).status("ON_SALE").build();
            OrderProductResponse product2 = anOrderProductResponse().productVariantId(2L).status("ON_SALE").build();
            OrderProductInfo expectedProductInfo1 = anOrderProductInfo().productVariantId(1L).build();
            OrderProductInfo expectedProductInfo2 = anOrderProductInfo().productVariantId(2L).build();
            given(orderProductAdaptor.getProducts(anyList())).willReturn(List.of(product1, product2));
            //when
            List<OrderProductInfo> result = orderProductService.getProducts(List.of(itemCommand1, itemCommand2));
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .isEqualTo(List.of(expectedProductInfo1, expectedProductInfo2));
        }

        @Test
        @DisplayName("상품 조회시 없는 상품이 있는 경우 예외를 던진다")
        void getProducts_not_found_product(){
            //given
            CreateOrderItemCommand itemCommand1 = OrderCommandFixture.anOrderItemCommand().productVariantId(1L).quantity(2).build();
            CreateOrderItemCommand itemCommand2 = OrderCommandFixture.anOrderItemCommand().productVariantId(2L).quantity(5).build();
            OrderProductResponse product = anOrderProductResponse().productVariantId(1L).status("ON_SALE").build();
            given(orderProductAdaptor.getProducts(anyList())).willReturn(List.of(product));
            //when
            //then
            assertThatThrownBy(() -> orderProductService.getProducts(List.of(itemCommand1, itemCommand2)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_PRODUCT_NOT_FOUND);
        }

        @Test
        @DisplayName("상품이 판매중이 아니라면 예외가 발생한다")
        void getProducts_product_not_on_sale(){
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
            assertThatThrownBy(() -> orderProductService.getProducts(List.of(itemCommand1, itemCommand2)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_PRODUCT_NOT_ON_SALE);
        }

        @Test
        @DisplayName("상품 수량이 부족하면 예외가 발생한다")
        void getProducts_product_quantity_insufficient(){
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
            assertThatThrownBy(() -> orderProductService.getProducts(List.of(itemCommand1, itemCommand2)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_PRODUCT_INSUFFICIENT_STOCK);
        }
    }
}

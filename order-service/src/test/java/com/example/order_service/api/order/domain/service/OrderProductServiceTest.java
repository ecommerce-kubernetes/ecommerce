package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.domain.service.dto.result.OrderProductInfo;
import com.example.order_service.api.order.facade.dto.command.CreateOrderItemCommand;
import com.example.order_service.api.order.infrastructure.client.product.OrderProductAdaptor;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderProductServiceTest {

    @InjectMocks
    private OrderProductService orderProductService;

    @Mock
    private OrderProductAdaptor orderProductAdaptor;

    private CreateOrderItemCommand createOrderItemCommand(Long productVariantId, int quantity) {
        return CreateOrderItemCommand.builder()
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();
    }

    private OrderProductResponse createOrderProductResponse(Long variantId, ProductStatus status, int stockQuantity) {
        return OrderProductResponse.builder()
                .productId(1L)
                .productVariantId(variantId)
                .status(status)
                .sku("TEST")
                .productName("상품")
                .unitPrice(
                        OrderProductResponse.UnitPrice.builder()
                                .originalPrice(10000L)
                                .discountRate(10)
                                .discountAmount(1000L)
                                .discountedPrice(9000L)
                                .build())
                .stockQuantity(stockQuantity)
                .thumbnailUrl("http://thumbnail.jpg")
                .productOptionInfos(List.of())
                .build();
    }

    @Nested
    @DisplayName("상품 정보 조회")
    class GetProducts {

        @Test
        @DisplayName("상품 정보를 조회한다")
        void getProducts(){
            //given
            CreateOrderItemCommand itemCommand1 = createOrderItemCommand(1L, 2);
            CreateOrderItemCommand itemCommand2 = createOrderItemCommand(2L, 5);
            OrderProductResponse product1 = createOrderProductResponse(1L, ProductStatus.ON_SALE, 100);
            OrderProductResponse product2 = createOrderProductResponse(2L, ProductStatus.ON_SALE, 100);
            given(orderProductAdaptor.getProducts(anyList())).willReturn(List.of(product1, product2));
            //when
            List<OrderProductInfo> result = orderProductService.getProducts(List.of(itemCommand1, itemCommand2));
            //then
            assertThat(result).hasSize(2)
                    .extracting(OrderProductInfo::getProductVariantId)
                    .containsExactlyInAnyOrder(1L, 2L);
        }

        @Test
        @DisplayName("상품 조회시 없는 상품이 있는 경우 예외를 던진다")
        void getProducts_not_found_product(){
            //given
            CreateOrderItemCommand itemCommand1 = createOrderItemCommand(1L, 2);
            CreateOrderItemCommand itemCommand2 = createOrderItemCommand(2L, 5);
            OrderProductResponse product = createOrderProductResponse(1L, ProductStatus.ON_SALE, 100);
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
            CreateOrderItemCommand itemCommand1 = createOrderItemCommand(1L, 2);
            CreateOrderItemCommand itemCommand2 = createOrderItemCommand(2L, 5);
            OrderProductResponse product1 = createOrderProductResponse(1L, ProductStatus.ON_SALE, 100);
            // 판매 중지된 상품
            OrderProductResponse product2 = createOrderProductResponse(2L, ProductStatus.STOP_SALE, 100);
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
            CreateOrderItemCommand itemCommand1 = createOrderItemCommand(1L, 10);
            CreateOrderItemCommand itemCommand2 = createOrderItemCommand(2L, 5);
            OrderProductResponse product1 = createOrderProductResponse(1L, ProductStatus.ON_SALE, 100);
            // 재고가 부족한 상품
            OrderProductResponse product2 = createOrderProductResponse(2L, ProductStatus.ON_SALE, 3);
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

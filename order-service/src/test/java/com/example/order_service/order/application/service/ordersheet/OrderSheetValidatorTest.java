package com.example.order_service.order.application.service.ordersheet;

import com.example.order_service.common.exception.application.BusinessException;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.dto.result.OrderProductStatus;
import com.example.order_service.order.application.service.ordersheet.dto.command.OrderSheetCommand;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.exception.OrderErrorCode;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;

@ExtendWith(MockitoExtension.class)
class OrderSheetValidatorTest {

    @InjectMocks
    private OrderSheetValidator validator;

    @Test
    @DisplayName("주문 상품의 조회결과가 모두 존재하는지 검증")
    void validate_missing_product() {
        //given
        OrderSheetCommand.OrderItem item1 = OrderSheetCommand.OrderItem.builder()
                .productVariantId(1L)
                .quantity(10)
                .build();
        OrderSheetCommand.OrderItem item2 = OrderSheetCommand.OrderItem.builder()
                .productVariantId(2L)
                .quantity(10)
                .build();
        OrderSheetCommand.Create command = OrderSheetCommand.Create.builder()
                .userId(100L)
                .cartCouponId(null)
                .items(List.of(item1, item2))
                .build();
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품 1", "/product/product");
        OrderProductResult.Info product = Instancio.of(OrderProductResult.Info.class)
                .set(field("productSnapshot"), productSnapshot)
                .set(field("status"), OrderProductStatus.ON_SALE)
                .set(field("stock"), 100)
                .create();
        OrderProductResult.ProductList productList = OrderProductResult.ProductList.builder()
                .products(List.of(product))
                .build();
        //when
        //then
        assertThatThrownBy(() -> validator.validate(productList, command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("주문 가능한 상품인지 검증")
    void validate_orderable() {
        //given
        OrderSheetCommand.OrderItem item = OrderSheetCommand.OrderItem.builder()
                .productVariantId(1L)
                .quantity(10)
                .build();
        OrderSheetCommand.Create command = OrderSheetCommand.Create.builder()
                .userId(100L)
                .cartCouponId(null)
                .items(List.of(item))
                .build();
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품 1", "/product/product");
        OrderProductResult.Info product = Instancio.of(OrderProductResult.Info.class)
                .set(field("productSnapshot"), productSnapshot)
                .set(field("status"), OrderProductStatus.STOP_SALE)
                .set(field("stock"), 100)
                .create();
        OrderProductResult.ProductList productList = OrderProductResult.ProductList.builder()
                .products(List.of(product))
                .build();
        //when
        //then
        assertThatThrownBy(() -> validator.validate(productList, command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_PRODUCT_UNORDERABLE);
    }

    @Test
    @DisplayName("재고가 충분한지 검증")
    void validate_stock() {
        //given
        OrderSheetCommand.OrderItem item = OrderSheetCommand.OrderItem.builder()
                .productVariantId(1L)
                .quantity(10)
                .build();
        OrderSheetCommand.Create command = OrderSheetCommand.Create.builder()
                .userId(100L)
                .cartCouponId(null)
                .items(List.of(item))
                .build();
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품 1", "/product/product");
        OrderProductResult.Info product = Instancio.of(OrderProductResult.Info.class)
                .set(field("productSnapshot"), productSnapshot)
                .set(field("status"), OrderProductStatus.ON_SALE)
                .set(field("stock"), 9)
                .create();
        OrderProductResult.ProductList productList = OrderProductResult.ProductList.builder()
                .products(List.of(product))
                .build();
        //when
        //then
        assertThatThrownBy(() -> validator.validate(productList, command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_PRODUCT_INSUFFICIENT_STOCK);
    }
}
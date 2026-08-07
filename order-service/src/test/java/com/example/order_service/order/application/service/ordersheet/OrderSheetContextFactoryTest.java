package com.example.order_service.order.application.service.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.port.dto.OrderCartItemsResult;
import com.example.order_service.order.application.port.dto.OrderProductStatus;
import com.example.order_service.order.application.port.dto.OrderProductsResult;
import com.example.order_service.order.application.port.dto.OrdererProfileResult;
import com.example.order_service.order.application.service.ordersheet.dto.command.CreateDirectOrderSheetCommand;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetContext;
import com.example.order_service.order.domain.vo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class OrderSheetContextFactoryTest {

    private final OrderSheetContextFactory contextFactory = new OrderSheetContextFactory();

    @Test
    @DisplayName("장바구니 주문서 생성 컨텍스트를 생성한다.")
    void createForCart() {
        //given
        OrdererProfileResult ordererProfile = createOrdererProfileResult();
        OrderCartItemsResult cartItems = createOrderCartItemsResult();
        OrderProductsResult products = createOrderProductsResult();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        Orderer expectedOrderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress expectedShippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");

        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        ProductOptionSnapshot productOption = ProductOptionSnapshot.of("사이즈", "XL");
        //when
        CreateOrderSheetContext context = contextFactory.createForCart(ordererProfile, cartItems, products, expiresAt);
        //then
        assertThat(context.orderer()).isEqualTo(expectedOrderer);
        assertThat(context.shippingAddress()).isEqualTo(expectedShippingAddress);
        assertThat(context.items()).hasSize(1)
                .extracting("productSnapshot", "priceSnapshot", "quantity", "optionSnapshots")
                .containsExactly(
                        tuple(productSnapshot, priceSnapshot, 3, List.of(productOption))
                );
        assertThat(context.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("즉시 주문서 생성 컨텍스트를 생성한다.")
    void createForDirect() {
        //given
        OrdererProfileResult ordererProfileResult = createOrdererProfileResult();
        CreateDirectOrderSheetCommand directCommand = createDirectCommand();
        OrderProductsResult productsResult = createOrderProductsResult();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        Orderer expectedOrderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress expectedShippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");

        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        ProductOptionSnapshot productOption = ProductOptionSnapshot.of("사이즈", "XL");
        //when
        CreateOrderSheetContext context = contextFactory.createForDirect(ordererProfileResult, directCommand, productsResult, expiresAt);
        //then
        assertThat(context.orderer()).isEqualTo(expectedOrderer);
        assertThat(context.shippingAddress()).isEqualTo(expectedShippingAddress);
        assertThat(context.items()).hasSize(1)
                .extracting("productSnapshot", "priceSnapshot", "quantity", "optionSnapshots")
                .containsExactly(
                        tuple(productSnapshot, priceSnapshot, 3, List.of(productOption))
                );
        assertThat(context.expiresAt()).isEqualTo(expiresAt);
    }

    private OrdererProfileResult createOrdererProfileResult() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345",
                "서울시 테헤란로 123", "123동 1234호");
        return OrdererProfileResult.builder()
                .orderer(orderer)
                .availablePoints(Money.wons(10000L))
                .defaultShippingAddress(shippingAddress)
                .build();
    }

    private OrderCartItemsResult createOrderCartItemsResult() {
        OrderCartItemsResult.Item item = OrderCartItemsResult.Item.builder()
                .cartItemId(1L)
                .productVariantId(1L)
                .quantity(3)
                .build();

        return OrderCartItemsResult.builder()
                .items(List.of(item))
                .build();
    }

    private OrderProductsResult createOrderProductsResult() {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        ProductOptionSnapshot productOption = ProductOptionSnapshot.of("사이즈", "XL");
        OrderProductsResult.OrderProductDetail product = OrderProductsResult.OrderProductDetail.builder()
                .productSnapshot(productSnapshot)
                .status(OrderProductStatus.ON_SALE)
                .stock(100)
                .priceSnapshot(priceSnapshot)
                .options(List.of(productOption))
                .build();
        return OrderProductsResult.builder()
                .products(List.of(product))
                .build();
    }

    private CreateDirectOrderSheetCommand createDirectCommand() {
        CreateDirectOrderSheetCommand.OrderVariant item = CreateDirectOrderSheetCommand.OrderVariant.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();

        return CreateDirectOrderSheetCommand.builder()
                .userId(1L)
                .items(List.of(item))
                .build();
    }
}
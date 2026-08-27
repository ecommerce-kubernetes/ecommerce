package com.example.order_service.order.application.service.ordersheet;

import com.example.order_service.order.application.port.dto.OrderCartItemsResult;
import com.example.order_service.order.application.port.dto.OrderProductsResult;
import com.example.order_service.order.application.port.dto.OrdererProfileResult;
import com.example.order_service.order.application.service.fixture.OrderCartResultFixture;
import com.example.order_service.order.application.service.fixture.OrderProductResultFixture;
import com.example.order_service.order.application.service.fixture.OrderSheetCommandFixture;
import com.example.order_service.order.application.service.fixture.OrderUserResultFixture;
import com.example.order_service.order.application.service.ordersheet.dto.command.CreateDirectOrderSheetCommand;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetContext;
import com.example.order_service.order.domain.vo.ShippingAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class OrderSheetContextFactoryTest {

    private final OrderSheetContextFactory contextFactory = new OrderSheetContextFactory();

    @Test
    @DisplayName("장바구니 주문서 생성 컨텍스트를 생성한다.")
    void createForCart() {
        //given
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        OrdererProfileResult ordererProfile = OrderUserResultFixture.anOrdererProfile().defaultShippingAddress(shippingAddress).build();

        OrderCartItemsResult cartItems = OrderCartResultFixture.anOrderCartItems().build();
        OrderProductsResult products = OrderProductResultFixture.anOrderProducts().build();

        OrderProductsResult.OrderProductDetail product = products.products().getFirst();
        OrderCartItemsResult.Item cartItem = cartItems.items().getFirst();

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        //when
        CreateOrderSheetContext context = contextFactory.createForCart(ordererProfile, cartItems, products, expiresAt);
        //then
        assertThat(context.orderer()).isEqualTo(ordererProfile.orderer());
        assertThat(context.shippingAddress()).isEqualTo(ordererProfile.defaultShippingAddress());
        assertThat(context.items()).hasSize(1)
                .extracting("productSnapshot", "priceSnapshot", "quantity", "optionSnapshots")
                .containsExactly(
                        tuple(product.productSnapshot(), product.priceSnapshot(), cartItem.quantity(), product.options())
                );
        assertThat(context.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("즉시 주문서 생성 컨텍스트를 생성한다.")
    void createForDirect() {
        //given
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        OrdererProfileResult ordererProfile = OrderUserResultFixture.anOrdererProfile().defaultShippingAddress(shippingAddress).build();

        CreateDirectOrderSheetCommand command = OrderSheetCommandFixture.anCreateDirectCommand().build();

        OrderProductsResult products = OrderProductResultFixture.anOrderProducts().build();

        OrderProductsResult.OrderProductDetail product = products.products().getFirst();
        CreateDirectOrderSheetCommand.OrderVariant item = command.items().getFirst();

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        //when
        CreateOrderSheetContext context = contextFactory.createForDirect(ordererProfile, command, products, expiresAt);
        //then
        assertThat(context.orderer()).isEqualTo(ordererProfile.orderer());
        assertThat(context.shippingAddress()).isEqualTo(shippingAddress);
        assertThat(context.items()).hasSize(1)
                .extracting("productSnapshot", "priceSnapshot", "quantity", "optionSnapshots")
                .containsExactly(
                        tuple(product.productSnapshot(), product.priceSnapshot(), item.quantity(), product.options())
                );
        assertThat(context.expiresAt()).isEqualTo(expiresAt);
    }
}
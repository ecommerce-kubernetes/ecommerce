package com.example.order_service.order.application.service.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.order.AppliedCartCoupon;
import com.example.order_service.order.domain.order.AppliedItemCoupon;
import com.example.order_service.order.domain.order.OrderAmount;
import com.example.order_service.order.domain.order.OrderItemAmount;
import com.example.order_service.order.domain.order.context.CreateOrderContext;
import com.example.order_service.order.domain.order.context.CreateOrderItemContext;
import com.example.order_service.order.domain.repository.OrderRepository;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.domain.vo.ShippingAddress;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IsolatedTest
@Transactional
public class OrderCommandServiceTest {

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문을 저장한다")
    void save() {
        //given
        CreateOrderContext context = createOrderContext();
        //when
        Long orderId = orderCommandService.saveOrder(context);
        //then
        assertThat(orderId).isNotNull();
    }

    private CreateOrderContext createOrderContext() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        AppliedCartCoupon appliedCartCoupon = AppliedCartCoupon.of(10L, "장바구니 1000원 할인 쿠폰");

        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        AppliedItemCoupon appliedItemCoupon = AppliedItemCoupon.of(1L, "1000원 할인 쿠폰");
        OrderItemAmount orderItemAmount = OrderItemAmount.of(
                Money.wons(30000L),
                Money.wons(3000L),
                Money.wons(27000L),
                Money.wons(1000L),
                Money.wons(26000L)
        );

        CreateOrderItemContext itemContext = CreateOrderItemContext.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .appliedItemCoupon(appliedItemCoupon)
                .quantity(3)
                .options(Collections.emptyList())
                .orderItemAmount(orderItemAmount)
                .build();

        OrderAmount orderAmount = OrderAmount.of(Money.wons(30000L), Money.wons(3000L), Money.wons(1000L),
                Money.wons(1000L), Money.wons(1000L), Money.wons(24000L));

        return CreateOrderContext.builder()
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .items(List.of(itemContext))
                .appliedCartCoupon(appliedCartCoupon)
                .orderAmount(orderAmount)
                .build();
    }
}

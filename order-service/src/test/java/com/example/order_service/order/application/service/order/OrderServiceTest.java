package com.example.order_service.order.application.service.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.dto.result.OrderResult;
import com.example.order_service.order.application.event.OrderCreatedEvent;
import com.example.order_service.order.application.service.order.dto.command.OrderContext;
import com.example.order_service.order.domain.model.OrderStatus;
import com.example.order_service.order.domain.repository.OrderRepository;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.support.annotation.MockKafka;
import com.example.order_service.support.annotation.MockRedis;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RecordApplicationEvents
@MockKafka
@MockRedis
@Transactional
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Nested
    @DisplayName("주문 저장")
    class Save {

        @Test
        @DisplayName("주문을 저장한다")
        void save() {
            //given
            OrderContext.CreateOrderContext context = createContext();
            //when
            OrderResult.Create result = orderService.saveOrder(context);
            //then
            assertThat(result.orderNo()).isNotNull();
            assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
            long eventCount = applicationEvents.stream(OrderCreatedEvent.class).count();
            assertThat(eventCount).isEqualTo(1);
            OrderCreatedEvent createdEvent = applicationEvents.stream(OrderCreatedEvent.class).findFirst().orElseThrow();
            assertThat(createdEvent.getOrderNo()).isEqualTo(result.orderNo());
        }
    }

    private OrderContext.CreateOrderContext createContext(){
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1-XL-BLUE", "청바지",
                "/product/product/jean_1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        ProductOptionSnapshot xl = ProductOptionSnapshot.of("사이즈", "XL");
        ProductOptionSnapshot blue = ProductOptionSnapshot.of("색상", "BLUE");
        OrderCouponSnapshot cartCoupon = OrderCouponSnapshot.of(1L, "장바구니 1000원 할인 쿠폰", Money.wons(1000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(2L, "하의 1000원 할인 쿠폰", Money.wons(1000L));
        OrderContext.ItemContext item = OrderContext.ItemContext.builder()
                .productSnapshot(productSnapshot)
                .itemPrice(priceSnapshot)
                .itemCoupon(itemCoupon)
                .quantity(1)
                .options(List.of(xl, blue))
                .build();

        return OrderContext.CreateOrderContext.builder()
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .orderItems(List.of(item))
                .cartCoupon(cartCoupon)
                .totalOriginalPrice(Money.wons(10000L))
                .totalProductDiscountAmount(Money.wons(1000L))
                .totalCouponDiscountAmount(Money.wons(2000L))
                .usedPoints(Money.wons(1000L))
                .totalPaymentAmount(Money.wons(6000L))
                .build();
    }
}

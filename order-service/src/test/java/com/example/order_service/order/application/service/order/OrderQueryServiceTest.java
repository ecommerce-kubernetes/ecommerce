package com.example.order_service.order.application.service.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.domain.model.Order;
import com.example.order_service.order.domain.model.OrderItem;
import com.example.order_service.order.domain.repository.OrderRepository;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.exception.OrderErrorCode;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RecordApplicationEvents
@IsolatedTest
@Transactional
public class OrderQueryServiceTest {

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private OrderRepository orderRepository;

    @Nested
    @DisplayName("주문 조회")
    class GetOrder {

        @Test
        @DisplayName("주문을 조회한다")
        void getOrder(){
            //given
            Order order = createOrder();
            orderRepository.save(order);
            //when
            OrderResult.Detail result = orderQueryService.getOrder(1L, 1L);
            //then
            assertThat(result.orderNo()).isEqualTo("orderNo");
            assertThat(result.orderer().getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("주문을 찾을 수 없으면 예외가 발생한다")
        void getOrder_not_found(){
            //given
            //when
            //then
            assertThatThrownBy(() -> orderQueryService.getOrder(999L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("주문 리스트 조회")
    class GetOrders {

        @Test
        @DisplayName("주문 리스트를 조회한다")
        void getOrders(){
            //given
            Order order1 = createOrder();
            Order order2 = createOrder();
            Pageable pageable = PageRequest.of(0, 20);
            OrderSearchCommand command = OrderSearchCommand.of("latest", null, null);
            ReflectionTestUtils.setField(order1, "orderNo", "ORD1");
            ReflectionTestUtils.setField(order2, "orderNo", "ORD2");
            orderRepository.saveAll(List.of(order1, order2));
            //when
            Page<OrderResult.Summary> orders = orderQueryService.getOrders(1L, command, pageable);
            //then
            assertThat(orders.getContent()).hasSize(2);
            assertThat(orders.getTotalPages()).isEqualTo(1);
            assertThat(orders.getContent())
                    .extracting("orderNo")
                    .containsExactlyInAnyOrder("ORD1", "ORD2");
        }
    }

    private Order createOrder() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345",
                "서울시 테헤란로 123", "123동 1234호");
//        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "장바구니 1000원 할인", Money.wons(1000L));
        OrderItem orderItem = createOrderItem();
        return Order.init("orderNo", orderer, shippingAddress, null, List.of(orderItem),
                Money.wons(10000L), Money.wons(1000L), Money.wons(2000L), Money.ZERO, Money.wons(7000L));
    }

    private OrderItem createOrderItem() {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1-XL-BLUE", "청바지",
                "/product/product/jean_1.jpg");
        ProductPriceSnapshot productPrice = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        ProductOptionSnapshot xl = ProductOptionSnapshot.of("사이즈", "XL");
        ProductOptionSnapshot blue = ProductOptionSnapshot.of("색상", "BLUE");
//        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "하의 1000원 할인 쿠폰", Money.wons(1000L));
        return null;
//        return OrderItem.create(productSnapshot, productPrice, itemCoupon, 1, List.of(xl, blue));
    }
}

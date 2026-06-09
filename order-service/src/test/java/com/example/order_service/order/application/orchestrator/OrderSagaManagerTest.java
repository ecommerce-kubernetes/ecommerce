package com.example.order_service.order.application.orchestrator;


import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.messaging.dto.SagaMessage;
import com.example.order_service.order.application.service.saga.OrderSagaService;
import com.example.order_service.order.domain.model.Order;
import com.example.order_service.order.domain.model.OrderItem;
import com.example.order_service.order.domain.model.OrderStatus;
import com.example.order_service.order.domain.repository.OrderRepository;
import com.example.order_service.order.domain.repository.OrderSagaInstanceRepository;
import com.example.order_service.order.domain.saga.OrderSagaInstance;
import com.example.order_service.order.domain.saga.SagaStatus;
import com.example.order_service.order.domain.saga.SagaStep;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.support.annotation.MockKafka;
import com.example.order_service.support.annotation.MockRedis;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@MockRedis
@MockKafka
class OrderSagaManagerTest {

    @Autowired
    private OrderSagaManager orderSagaManager;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderSagaInstanceRepository sagaRepository;
    @MockitoSpyBean
    private OrderSagaService orderSagaService;

    @AfterEach
    void cleanUp() {
        orderRepository.deleteAll();
        sagaRepository.deleteAll();
    }

    @Nested
    @DisplayName("SAGA 시작")
    class StartSaga {

        @Test
        @DisplayName("주문을 결제 상태로 변경하고 재고 차감 Saga 인스턴스를 생성한다")
        @Transactional
        void startSaga() {
            //given
            String orderNo = "orderNo";
            Order order = createOrder(orderNo);
            orderRepository.save(order);
            //when
            orderSagaManager.startSaga(orderNo);
            //then
            Order findOrder = orderRepository.findByOrderNo(orderNo).orElseThrow();
            OrderSagaInstance findSaga = sagaRepository.findByOrderNo(orderNo).orElseThrow();
            assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(findSaga.getCurrentStep()).isEqualTo(SagaStep.INVENTORY_DEDUCT_PENDING);
            assertThat(findSaga.getStatus()).isEqualTo(SagaStatus.STARTED);
        }

        @Test
        @DisplayName("Saga 인스턴스 생성 오류 발생시 주문 상태는 롤백되어야 한다")
        void startSaga_rollback_order_status_saga_creation_fail() {
            //given
            String orderNo = "orderNo";
            Order order = createOrder(orderNo);
            orderRepository.save(order);
            doThrow(new RuntimeException())
                    .when(orderSagaService).createSaga(any());
            //when
            assertThatThrownBy(() -> orderSagaManager.startSaga(orderNo))
                    .isInstanceOf(RuntimeException.class);
            //then
            Order findOrder = orderRepository.findByOrderNo(orderNo).orElseThrow();
            assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(sagaRepository.findByOrderNo(orderNo)).isEmpty();
        }

        private Order createOrder(String orderNo) {
            Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
            ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
            OrderCouponSnapshot cartCoupon = OrderCouponSnapshot.of(1L, "장바구니 1000원 할인", Money.wons(1000L));
            List<OrderItem> orderItems = createOrderItems();
            return Order.init(orderNo, orderer, shippingAddress, cartCoupon, orderItems, Money.wons(10000L),
                    Money.wons(1000L), Money.wons(2000L), Money.wons(1000L), Money.wons(6000L));
        }

        private List<OrderItem> createOrderItems() {
            ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
            ProductPriceSnapshot productPriceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
            OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(2L, "상품 1000원 할인 쿠폰", Money.wons(1000L));
            OrderItem orderItem = OrderItem.create(productSnapshot, productPriceSnapshot, itemCoupon, 1, List.of());
            return List.of(orderItem);
        }
    }

    @Nested
    @DisplayName("Saga 메시지 수신")
    class HandleReply {

    }
}
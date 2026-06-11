package com.example.order_service.order.application.messaging;

import com.example.order_service.order.application.event.OrderSagaFailedEvent;
import com.example.order_service.order.application.event.OrderSagaProcessEvent;
import com.example.order_service.payment.application.service.PaymentFacade;
import com.example.order_service.support.annotation.MockKafka;
import com.example.order_service.support.annotation.MockRedis;
import com.example.order_service.support.config.TestAsyncConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = TestAsyncConfig.class)
@MockRedis
@MockKafka
class SagaEventListenerTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @MockitoBean
    private SagaMessageDispatcher dispatcher;
    @MockitoBean
    private PaymentFacade paymentFacade;

    @Test
    @DisplayName("주문 SAGA 진행 메시지 수신시 dispatcher 호출")
    @Transactional
    void onSagaProcess(){
        //given
        String orderNo = "orderNo";
        OrderSagaProcessEvent event = fixtureMonkey.giveMeBuilder(OrderSagaProcessEvent.class)
                .set("orderNo", orderNo)
                .sample();
        //when
        eventPublisher.publishEvent(event);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        //then
        verify(dispatcher, times(1)).dispatch(any());
    }

    @Test
    @DisplayName("주문 SAGA 실패 메시지 수신시 결제 취소 호출")
    @Transactional
    void onPaymentRefund(){
        //given
        String orderNo = "orderNo";
        OrderSagaFailedEvent event = OrderSagaFailedEvent.of(orderNo, "INSUFFICIENT_STOCK");
        //when
        eventPublisher.publishEvent(event);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        //then
        verify(paymentFacade, times(1)).revert(anyString(), anyString());
    }
}
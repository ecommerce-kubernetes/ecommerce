package com.example.order_service.order.application.messaging;

import com.example.order_service.order.application.event.OrderSagaFailedEvent;
import com.example.order_service.order.application.event.OrderSagaProcessEvent;
import com.example.order_service.payment.application.service.PaymentFacade;
import com.example.order_service.support.annotation.MockKafka;
import com.example.order_service.support.annotation.MockRedis;
import com.example.order_service.support.config.TestAsyncConfig;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.mockito.ArgumentMatchers.*;
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
        OrderSagaProcessEvent event = Instancio.create(OrderSagaProcessEvent.class);
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
        OrderSagaFailedEvent event = Instancio.create(OrderSagaFailedEvent.class);
        //when
        eventPublisher.publishEvent(event);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        //then
        verify(paymentFacade, times(1)).revert(anyLong(), anyString());
    }
}
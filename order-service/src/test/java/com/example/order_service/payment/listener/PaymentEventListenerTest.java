package com.example.order_service.payment.listener;

import com.example.order_service.order.application.orchestrator.OrderSagaManager;
import com.example.order_service.payment.application.event.PaymentCompleteEvent;
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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = TestAsyncConfig.class)
@MockRedis
@MockKafka
public class PaymentEventListenerTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockitoBean
    private OrderSagaManager orderSagaManager;

    @Test
    @DisplayName("결제 성공 이벤트 수신시 Saga 시작 호출")
    @Transactional
    void onPaymentComplete(){
        //given
        String orderNo = "orderNo";
        PaymentCompleteEvent paymentCompleteEvent = PaymentCompleteEvent.of(orderNo);
        //when
        eventPublisher.publishEvent(paymentCompleteEvent);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        //then
        verify(orderSagaManager, times(1)).startSaga(orderNo);
    }
}

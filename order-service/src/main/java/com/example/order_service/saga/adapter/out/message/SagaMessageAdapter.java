package com.example.order_service.saga.adapter.out.message;

import com.example.order_service.saga.adapter.out.message.processor.SagaMessageProcessor;
import com.example.order_service.saga.domain.event.SagaEvent;
import com.example.order_service.saga.exception.ProcessorNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SagaMessageAdapter {

    private final List<SagaMessageProcessor> processors;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSagaEvent(SagaEvent event) {
        SagaMessageProcessor processor = getProcessor(event);
        processor.process(event);
    }

    private SagaMessageProcessor getProcessor(SagaEvent event) {
        return this.processors.stream()
                .filter(processor -> processor.supports(event))
                .findFirst()
                .orElseThrow(() -> new ProcessorNotFoundException("사가 메시지 프로세서를 찾을 수 없습니다."));
    }
}

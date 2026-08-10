package com.example.order_service.payment.infrastructure.adaptor.event;

import com.example.order_service.payment.application.port.PaymentEventPort;
import com.example.order_service.payment.domain.event.PaymentApprovedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentEventAdaptor implements PaymentEventPort {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publishApproved(PaymentApprovedEvent event) {
        publisher.publishEvent(event);
    }
}

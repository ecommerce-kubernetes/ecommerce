package com.example.order_service.payment.infrastructure.adaptor.client;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.PortException;
import com.example.order_service.payment.application.port.PaymentPGPort;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.domain.PaymentProvider;
import com.example.order_service.payment.exception.PaymentPGPortErrorCode;
import com.example.order_service.payment.infrastructure.adaptor.client.pg.PGProcessor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentPGAdaptor implements PaymentPGPort {

    private final Map<PaymentProvider, PGProcessor> pgProcessorMap;

    public PaymentPGAdaptor(List<PGProcessor> pgProcessorList) {
        this.pgProcessorMap = pgProcessorList.stream()
                .collect(Collectors.toMap(PGProcessor::getSupportedProvider, Function.identity()));
    }

    @Override
    public PGConfirmResult confirm(Long orderId, String paymentKey, Money amount, PaymentProvider provider) {
        PGProcessor processor = getProcessor(provider);
        return processor.confirm(orderId, paymentKey, amount);
    }

    @Override
    public void netCancel(String paymentKey, String cancelReason, PaymentProvider provider) {
        PGProcessor processor = getProcessor(provider);
        processor.netCancel(paymentKey, cancelReason);
    }

    private PGProcessor getProcessor(PaymentProvider provider) {
        PGProcessor pgProcessor = pgProcessorMap.get(provider);
        if (pgProcessor == null) {
            throw new PortException(PaymentPGPortErrorCode.UNSUPPORTED_PROVIDER, "UNSUPPORTED_PROVIDER", "지원하지 않는 결제사 입니다.");
        }
        return pgProcessor;
    }
}

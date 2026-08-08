package com.example.order_service.payment.infrastructure.adaptor.client;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.PortException;
import com.example.order_service.infrastructure.gateway.PGGateway;
import com.example.order_service.payment.application.port.PaymentPGPort;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.domain.PaymentProvider;
import com.example.order_service.payment.exception.PaymentPGPortErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentPGAdaptor implements PaymentPGPort {

    private final Map<PaymentProvider, PGGateway> gatewayMap;

    public PaymentPGAdaptor(List<PGGateway> gateways) {
        this.gatewayMap = gateways.stream()
                .collect(Collectors.toMap(
                        PGGateway::getSupportedProvider,
                        Function.identity()
                ));
    }

    @Override
    public PGConfirmResult confirm(Long orderId, String paymentKey, Money amount, PaymentProvider provider) {
        return null;
    }

    private PGGateway getGateway(PaymentProvider provider) {
        PGGateway pgGateway = this.gatewayMap.get(provider);
        if (pgGateway == null) {
            throw new PortException(PaymentPGPortErrorCode.UNSUPPORTED_PROVIDER, "UNSUPPORTED_PG", "지원하지 않는 결제사");
        }

        return pgGateway;
    }
}

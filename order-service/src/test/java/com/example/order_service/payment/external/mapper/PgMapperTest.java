package com.example.order_service.payment.external.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.payment.application.external.dto.result.PgPaymentResult;
import com.example.order_service.payment.application.external.mapper.PgMapper;
import com.example.order_service.payment.application.external.mapper.PgMapperImpl;
import com.example.order_service.payment.domain.model.PaymentMethod;
import com.example.order_service.payment.domain.model.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class PgMapperTest {

    private final MoneyMapper moneyMapper = Mappers.getMapper(MoneyMapper.class);
    private final PgMapper mapper = new PgMapperImpl(moneyMapper);

    @Test
    @DisplayName("토스 결제 승인 응답을 result로 매핑")
    void toResult_tossConfirmResponse() {
        //given
        OffsetDateTime approvedAt = OffsetDateTime.now();
        LocalDateTime expectedDateTime = approvedAt.toLocalDateTime();
        TossClientResponse.Confirm response = TossClientResponse.Confirm.builder()
//                .paymentKey("paymentKey")
//                .orderId("orderNo")
                .totalAmount(10000L)
                .status("DONE")
                .method("CARD")
                .approvedAt(approvedAt)
                .build();

        PgPaymentResult.Approval expected = PgPaymentResult.Approval.builder()
                .orderNo("orderNo")
                .paymentKey("paymentKey")
                .totalAmount(Money.wons(10000L))
                .status(PaymentStatus.DONE)
                .method(PaymentMethod.CARD)
                .approvedAt(expectedDateTime)
                .build();
        //when
        PgPaymentResult.Approval result = mapper.toResult(response);
        //then
        assertThat(result).usingRecursiveComparison()
                .isEqualTo(expected);
    }
}

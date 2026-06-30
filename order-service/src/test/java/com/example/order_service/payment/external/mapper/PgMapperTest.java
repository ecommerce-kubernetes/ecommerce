package com.example.order_service.payment.external.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.payment.application.external.dto.result.PGPaymentResult;
import com.example.order_service.payment.application.external.mapper.PgMapper;
import com.example.order_service.payment.application.external.mapper.PgMapperImpl;
import com.example.order_service.payment.domain.model.PaymentMethod;
import com.example.order_service.payment.domain.model.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

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
                .status("DONE")
                .totalAmount(10000L)
                .method("카드")
                .lastTransactionKey("9C62B18EEF0DE3EB7F4422EB6D14BC6E")
                .approvedAt(approvedAt)
                .build();

        PGPaymentResult.Approval expected = PGPaymentResult.Approval.builder()
                .status(PaymentStatus.DONE)
                .totalAmount(Money.wons(10000L))
                .method(PaymentMethod.CARD)
                .transactionKey("9C62B18EEF0DE3EB7F4422EB6D14BC6E")
                .approvedAt(expectedDateTime)
                .build();
        //when
        PGPaymentResult.Approval result = mapper.toResult(response);
        //then
        assertThat(result).usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("토스 결제 취소 응답을 result로 매핑")
    void toResult_tossCancelResponse() {
        //given
        OffsetDateTime canceledAt = OffsetDateTime.now();
        LocalDateTime expectedDateTime = canceledAt.toLocalDateTime();
        TossClientResponse.CancelReceipt cancelReceipt = TossClientResponse.CancelReceipt.builder()
                .transactionKey("090A796806E726BBB929F4A2CA7DB9A7")
                .cancelAmount(10000L)
                .canceledAt(canceledAt)
                .cancelReason("테스트 결제 취소")
                .build();
        TossClientResponse.Cancel response = TossClientResponse.Cancel.builder()
                .status("CANCELED")
                .cancels(List.of(cancelReceipt))
                .build();

        PGPaymentResult.CancelReceipt expectedReceipt = PGPaymentResult.CancelReceipt.builder()
                .transactionKey("090A796806E726BBB929F4A2CA7DB9A7")
                .cancelAmount(Money.wons(10000L))
                .canceledAt(expectedDateTime)
                .cancelReason("테스트 결제 취소")
                .build();
        PGPaymentResult.Cancellation expectedResult = PGPaymentResult.Cancellation.builder()
                .status(PaymentStatus.CANCELED)
                .cancels(List.of(expectedReceipt))
                .build();
        //when
        PGPaymentResult.Cancellation result = mapper.toResult(response);
        //then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("토스 조회 응답을 매핑한다")
    void toResult_tossInquiryResponse(){
        //given
        OffsetDateTime approvedAt = OffsetDateTime.now();
        LocalDateTime expectedDateTime = approvedAt.toLocalDateTime();
        TossClientResponse.Inquiry response = TossClientResponse.Inquiry.builder()
                .paymentKey("paymentKey")
                .orderId("orderNo")
                .status("DONE")
                .totalAmount(1000L)
                .balanceAmount(1000L)
                .method("카드")
                .lastTransactionKey("transactionKey")
                .approvedAt(approvedAt)
                .build();

        PGPaymentResult.Inquiry expected = PGPaymentResult.Inquiry.builder()
                .paymentKey("paymentKey")
                .orderNo("orderNo")
                .status(PaymentStatus.DONE)
                .totalAmount(Money.wons(1000L))
                .balanceAmount(Money.wons(1000L))
                .method(PaymentMethod.CARD)
                .lastTransactionKey("transactionKey")
                .approvedAt(expectedDateTime)
                .build();

        //when
        PGPaymentResult.Inquiry result = mapper.toResult(response);
        //then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
}

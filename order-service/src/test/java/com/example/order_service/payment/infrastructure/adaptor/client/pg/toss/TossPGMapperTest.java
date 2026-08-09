package com.example.order_service.payment.infrastructure.adaptor.client.pg.toss;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.infrastructure.dto.response.pg.TossConfirmResponse;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.application.port.dto.PaymentPGStatus;
import com.example.order_service.payment.domain.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TossPGMapperTest {

    private final TossPGMapper mapper = new TossPGMapper();

    @Test
    @DisplayName("결제 승인 결과로 토스 승인 응답을 매핑한다.")
    void toConfirmResult(){
        //given
        OffsetDateTime approvedAt = OffsetDateTime.now();
        LocalDateTime expectedApprovedAt = approvedAt.toLocalDateTime();
        TossConfirmResponse response = TossConfirmResponse.builder()
                .status("DONE")
                .totalAmount(1000L)
                .method("카드")
                .lastTransactionKey("transactionKey")
                .approvedAt(approvedAt)
                .build();
        //when
        PGConfirmResult confirmResult = mapper.toConfirmResult(response);
        //then
        assertThat(confirmResult.status()).isEqualTo(PaymentPGStatus.DONE);
        assertThat(confirmResult.amount()).isEqualTo(Money.wons(1000L));
        assertThat(confirmResult.method()).isEqualTo(PaymentMethod.CARD);
        assertThat(confirmResult.transactionKey()).isEqualTo("transactionKey");
        assertThat(confirmResult.approvedAt()).isEqualTo(expectedApprovedAt);
    }

}
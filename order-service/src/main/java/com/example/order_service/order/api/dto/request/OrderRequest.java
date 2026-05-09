package com.example.order_service.order.api.dto.request;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.dto.command.OrderCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

public class OrderRequest {

    @Builder
    public record Create(
            @NotNull(message = "주문서 ID는 필수 입니다")
            Long orderSheetId,
            @Valid
            @NotNull(message = "배송지 정보는 필수 입니다")
            Delivery deliveryAddress,
            Long couponId,
            @NotNull(message = "사용할 포인트는 필수 입니다")
            @Min(value = 0, message = "사용할 포인트는 0 이상이여야 합니다")
            Long pointToUse,
            @Min(value = 1, message = "예상 결제 금액은 1 이상이여야 합니다")
            Long expectedPrice
    ) {
        public OrderCommand.Create toCommand(Long userId) {
            return OrderCommand.Create.builder()
                    .userId(userId)
                    .orderSheetId(orderSheetId)
                    .deliveryAddress(deliveryAddress.toCommand())
                    .couponId(couponId)
                    .pointToUse(Money.wons(pointToUse))
                    .expectedPrice(Money.wons(expectedPrice))
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record Delivery(
            @NotBlank(message = "수령인 이름은 필수 입니다")
            String receiverName,
            @NotBlank(message = "수령인 연락처는 필수 입니다")
            @Pattern(regexp = "^01[016-9]-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
            String receiverPhone,
            @NotBlank(message = "우편 번호는 필수 입니다")
            String zipCode,
            @NotBlank(message = "기본 주소는 필수 입니다")
            String baseAddress,
            @NotBlank(message = "상세 주소는 필수 입니다")
            String detailAddress
    ) {
        public OrderCommand.Delivery toCommand() {
            return OrderCommand.Delivery.builder()
                    .receiverName(receiverName)
                    .receiverPhone(receiverPhone)
                    .zipCode(zipCode)
                    .baseAddress(baseAddress)
                    .detailAddress(detailAddress)
                    .build();
        }
    }
}

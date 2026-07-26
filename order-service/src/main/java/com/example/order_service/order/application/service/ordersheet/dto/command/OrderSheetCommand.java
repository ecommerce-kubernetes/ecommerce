package com.example.order_service.order.application.service.ordersheet.dto.command;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

@Deprecated
public class OrderSheetCommand {
    @Builder
    public record UpdatePoints(
            String sheetId,
            Long userId,
            Money usedPoints
    ) {
        public static UpdatePoints of(String sheetId, Long userId, Long usedPoints) {
            return UpdatePoints.builder()
                    .sheetId(sheetId)
                    .userId(userId)
                    .usedPoints(Money.wons(usedPoints))
                    .build();
        }
    }

}

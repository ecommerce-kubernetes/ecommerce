package com.example.order_service.order.application.service.ordersheet.dto.command;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.Builder;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

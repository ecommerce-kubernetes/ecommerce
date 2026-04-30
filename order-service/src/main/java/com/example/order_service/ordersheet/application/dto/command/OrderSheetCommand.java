package com.example.order_service.ordersheet.application.dto.command;

import com.example.order_service.api.common.exception.business.BusinessException;
import com.example.order_service.api.common.exception.business.code.OrderSheetErrorCode;
import lombok.Builder;

import java.util.List;

public class OrderSheetCommand {

    @Builder
    public record Create(
            Long userId,
            List<OrderItem> items
    ) {
        public Create {
            if (items == null || items.isEmpty()) {
                throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_ITEM_REQUIRED);
            }
            //variantIds 중복 검사
            long uniqueItemCount = items.stream().map(OrderItem::productVariantId)
                    .distinct()
                    .count();
            if (uniqueItemCount != items.size()) {
                throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_DUPLICATE_ITEMS);
            }
        }
    }

    @Builder
    public record OrderItem(
            Long productVariantId,
            Integer quantity
    ) {
    }
}

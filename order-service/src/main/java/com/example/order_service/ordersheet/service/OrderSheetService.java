package com.example.order_service.ordersheet.service;

import com.example.order_service.ordersheet.service.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.service.dto.result.OrderSheetResult;
import org.springframework.stereotype.Service;

@Service
public class OrderSheetService {

    public OrderSheetResult.Default createOrderSheet(OrderSheetCommand.Create command) {
        return null;
    }
}

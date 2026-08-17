package com.example.order_service.saga.adapter.out.message;

import com.example.order_service.saga.adapter.out.message.processor.CouponMessageProcessor;
import com.example.order_service.saga.adapter.out.message.processor.InventoryMessageProcessor;
import com.example.order_service.saga.adapter.out.message.processor.PointMessageProcessor;
import com.example.order_service.saga.domain.event.*;
import com.example.order_service.saga.exception.ProcessorNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SagaMessageAdapterTest {

}
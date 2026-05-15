package com.example.order_service.ordersheet.application;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetCouponResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetResult;
import com.example.order_service.ordersheet.application.external.OrderSheetCouponGateway;
import com.example.order_service.ordersheet.application.external.OrderSheetProductGateway;
import com.example.order_service.ordersheet.domain.model.OrderSheet;
import com.example.order_service.ordersheet.domain.model.vo.ProductStatus;
import com.example.order_service.ordersheet.domain.repository.OrderSheetRepository;
import com.example.order_service.ordersheet.exception.OrderSheetErrorCode;
import com.example.order_service.ordersheet.infrastructure.config.OrderSheetProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static com.example.order_service.support.TestFixtureUtil.sample;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderSheetAppServiceTest {
    @InjectMocks
    private OrderSheetAppService orderSheetAppService;
    @Mock
    private OrderSheetProductGateway orderSheetProductGateway;
    @Mock
    private OrderSheetCouponGateway orderSheetCouponGateway;
    @Mock
    private OrderSheetRepository repository;
    @Spy
    private OrderSheetProperties properties = new OrderSheetProperties(30L);

    @Nested
    @DisplayName("주문서 저장")
    class Create {

    }
}

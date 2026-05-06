package com.example.order_service.docs.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.docs.descriptor.OrderSheetDescriptor;
import com.example.order_service.ordersheet.api.OrderSheetController;
import com.example.order_service.ordersheet.api.dto.request.OrderSheetRequest;
import com.example.order_service.ordersheet.api.dto.response.OrderSheetResponse;
import com.example.order_service.ordersheet.application.OrderSheetAppService;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetResult;
import com.example.order_service.support.RestDocSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderSheetControllerDocsTest extends RestDocSupport {
    private OrderSheetAppService orderSheetAppService = mock(OrderSheetAppService.class);

    @Override
    protected String getTag() {
        return "ORDER-SHEETS";
    }

    @Override
    protected Object initController() {
        return new OrderSheetController(orderSheetAppService);
    }

    @Nested
    @DisplayName("주문서 생성 API")
    class CreateOrderSheet {
        @Test
        @DisplayName("주문서 생성 API")
        void createOrderSheet() throws Exception {
            //given
            OrderSheetRequest.OrderItem item = OrderSheetRequest.OrderItem.builder()
                    .productVariantId(1L)
                    .quantity(2)
                    .build();
            OrderSheetRequest.Create request = OrderSheetRequest.Create.builder()
                    .items(List.of(item))
                    .build();
            OrderSheetResult.Default result = createOrderSheetResult();
            HttpHeaders roleUser = createAuthHeader("ROLE_USER");
            given(orderSheetAppService.createOrderSheet(any(OrderSheetCommand.Create.class)))
                    .willReturn(result);
            OrderSheetResponse.Create response = OrderSheetResponse.Create.from(result);
            //when
            //then
            mockMvc.perform(post("/order-sheets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .headers(roleUser)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)))
                    .andDo(createSecuredDocument("04-ordersheet-01-create",
                            "주문서 저장",
                            "주문서를 저장한다",
                            OrderSheetDescriptor.getCreateRequest(),
                            OrderSheetDescriptor.getCreateResponse()));
        }

        private OrderSheetResult.Default createOrderSheetResult() {
            OrderSheetResult.OrderItemPrice unitPrice = OrderSheetResult.OrderItemPrice.builder()
                    .originalPrice(Money.wons(10000L))
                    .discountRate(10)
                    .discountAmount(Money.wons(1000L))
                    .discountedPrice(Money.wons(9000L))
                    .build();
            OrderSheetResult.OrderItemOption option = OrderSheetResult.OrderItemOption.builder()
                    .optionTypeName("색상")
                    .optionValueName("RED")
                    .build();
            OrderSheetResult.Summary summary = OrderSheetResult.Summary.builder()
                    .totalOriginPrice(Money.wons(20000L))
                    .totalProductDiscount(Money.wons(2000L))
                    .totalBasePaymentAmount(Money.wons(18000L))
                    .build();

            OrderSheetResult.OrderItem orderItem = OrderSheetResult.OrderItem.builder()
                    .productId(1L)
                    .productVariantId(1L)
                    .productName("상품 1")
                    .thumbnail("/product/product/test.jpg")
                    .quantity(2)
                    .unitPrice(unitPrice)
                    .options(List.of(option))
                    .build();

            return OrderSheetResult.Default.builder()
                    .sheetId("sheetId")
                    .expiresAt(LocalDateTime.now())
                    .summary(summary)
                    .items(List.of(orderItem))
                    .build();
        }
    }
}

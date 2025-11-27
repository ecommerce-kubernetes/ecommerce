package com.example.order_service.docs.order;

import com.example.order_service.controller.OrderController;
import com.example.order_service.controller.util.validator.OrderPageableValidator;
import com.example.order_service.controller.util.validator.PageableValidatorFactory;
import com.example.order_service.docs.RestDocSupport;
import com.example.order_service.dto.response.*;
import com.example.order_service.entity.DomainType;
import com.example.order_service.service.OrderService;
import com.example.order_service.service.SseConnectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderControllerDocsTest extends RestDocSupport {

    private OrderService orderService = mock(OrderService.class);
    private SseConnectionService sseConnectionService = mock(SseConnectionService.class);
    private PageableValidatorFactory factory = mock(PageableValidatorFactory.class);

    @BeforeEach
    void validatorSetUp(){
        given(factory.getValidator(DomainType.ORDER))
                .willReturn(new OrderPageableValidator());
    }

    @Override
    protected Object initController() {
        return new OrderController(orderService, sseConnectionService, factory);
    }

    @Test
    @DisplayName("주문 목록 조회 API")
    void getOrders() throws Exception {
        //given
        LocalDateTime createAt = LocalDateTime.of(2025, 11, 27, 15, 30, 30);
        OrderItemResponse orderItem = createOrderItemResponse(1L, "상품1", "http://product1.jpg");
        OrderResponse orderResponse = createOrderResponse(1L, "COMPLETED", createAt, List.of(orderItem));
        given(orderService.getOrderList(any(Pageable.class), anyLong(), anyString(), anyString()))
                .willReturn(
                        PageDto.<OrderResponse>builder()
                                .content(List.of(orderResponse))
                                .currentPage(0)
                                .totalPage(10)
                                .pageSize(10)
                                .totalElement(100)
                                .build());
        //when
        //then
        mockMvc.perform(get("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("getOrderList",
                        preprocessResponse(prettyPrint())
                ));
    }

    private OrderItemResponse createOrderItemResponse(Long productId, String productName, String thumbNailUrl){
        return OrderItemResponse.builder()
                .productId(productId)
                .productName(productName)
                .thumbNailUrl(thumbNailUrl)
                .quantity(2)
                .unitPriceInfo(new UnitPriceInfo(3000, 10, 300, 2700))
                .totalPrice(5700)
                .options(List.of(new ItemOptionResponse("사이즈", "XL")))
                .build();
    }

    private OrderResponse createOrderResponse(Long orderId, String status, LocalDateTime createAt, List<OrderItemResponse> orderItemResponses){
        return OrderResponse.builder()
                .id(orderId)
                .status(status)
                .createAt(createAt)
                .orderItems(orderItemResponses)
                .build();
    }

}

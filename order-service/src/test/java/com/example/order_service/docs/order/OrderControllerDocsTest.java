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
import org.springframework.restdocs.payload.JsonFieldType;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
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
                        .header("X-User-Id", "1")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,asc")
                        .param("keyword", "keyword")
                        .param("year", "2025"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("getOrderList",
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("X-User-Id").description("회원 Id(회원 식별자)")
                        ),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호(0-based)"),
                                parameterWithName("size").description("페이지 크기"),
                                parameterWithName("sort").description("정렬 기준 속성, 뱡향 (예: id,asc)"),
                                parameterWithName("year").description("조회 연도"),
                                parameterWithName("keyword").description("검색 키워드")
                        ),
                        responseFields(
                                fieldWithPath("content[].id").description("주문 ID"),
                                fieldWithPath("content[].status").description("주문 상태"),
                                fieldWithPath("content[].createAt").description("주문 생성 시각"),
                                fieldWithPath("content[].orderItems[].productId").description("주문 상품 ID(상품(Product) 식별자)"),
                                fieldWithPath("content[].orderItems[].productName").description("주문 상품 이름"),
                                fieldWithPath("content[].orderItems[].thumbNailUrl").description("주문 상품 썸네일"),
                                fieldWithPath("content[].orderItems[].quantity").description("주문 수량"),
                                fieldWithPath("content[].orderItems[].unitPriceInfo.originalPrice").description("주문 상품 원본 가격"),
                                fieldWithPath("content[].orderItems[].unitPriceInfo.discountRate").description("상품 할인율"),
                                fieldWithPath("content[].orderItems[].unitPriceInfo.discountAmount").description("상품 할인 금액"),
                                fieldWithPath("content[].orderItems[].unitPriceInfo.discountedPrice").description("할인된 가격"),
                                fieldWithPath("content[].orderItems[].lineTotal").description("주문 항목 총액"),
                                fieldWithPath("content[].orderItems[].options[].optionTypeName").description("주문 상품 옵션 타입 (예: 사이즈)"),
                                fieldWithPath("content[].orderItems[].options[].optionValueName").description("주문 상품 옵션 값 (예: XL)"),
                                fieldWithPath("currentPage").description("현재 페이지"),
                                fieldWithPath("totalPage").description("총 페이지"),
                                fieldWithPath("pageSize").description("페이지 사이즈"),
                                fieldWithPath("totalElement").description("총 Element")
                        )
                ));
    }

    private OrderItemResponse createOrderItemResponse(Long productId, String productName, String thumbNailUrl){
        return OrderItemResponse.builder()
                .productId(productId)
                .productName(productName)
                .thumbNailUrl(thumbNailUrl)
                .quantity(2)
                .unitPriceInfo(new UnitPriceInfo(3000, 10, 300, 2700))
                .lineTotal(5700)
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

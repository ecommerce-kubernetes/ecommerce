package com.example.product_service.controller;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.variant.UpdateProductVariantRequest;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.service.ProductVariantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.example.product_service.controller.util.ControllerTestHelper.*;
import static com.example.product_service.controller.util.MessagePath.*;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductVariantController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductVariantControllerTest {

    private static final String BASE_PATH = "/variants";
    private static final String ID_PATH = BASE_PATH + "/1";

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    MessageSourceUtil ms;
    @MockitoBean
    ProductVariantService service;

    @BeforeEach
    void setUpMessages() {
        when(ms.getMessage(BAD_REQUEST)).thenReturn("BadRequest");
        when(ms.getMessage(BAD_REQUEST_VALIDATION)).thenReturn("Validation Error");
        when(ms.getMessage(CONFLICT)).thenReturn("Conflict");
    }

    @Test
    @DisplayName("상품 변형 수정 테스트-성공")
    void updateProductVariantTest_success() throws Exception {
        UpdateProductVariantRequest request = createUpdateProductVariantRequest();
        ProductVariantResponse response = new ProductVariantResponse(1L, "TS-RED-XL", 3000, 100, 10,
                List.of(new OptionValueResponse(1L, 1L, "value1"), new OptionValueResponse(2L, 1L, "value2")));

        when(service.updateVariantById(anyLong(), any(UpdateProductVariantRequest.class)))
                .thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifySuccessResponse(perform, status().isOk(), response);
    }

    @Test
    @DisplayName("상품 변형 수정 테스트-실패(검증)")
    void updateProductVariantTest_validation() throws Exception {
        UpdateProductVariantRequest request = new UpdateProductVariantRequest(-199, -1, 120, List.of(1L, 2L));
        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), ms.getMessage(BAD_REQUEST),
                ms.getMessage(BAD_REQUEST_VALIDATION), ID_PATH);
    }

    @Test
    @DisplayName("상품 변형 수정 테스트-실패(없음)")
    void updateProductVariantTest_notFound() throws Exception {
        UpdateProductVariantRequest request = createUpdateProductVariantRequest();

        when(service.updateVariantById(anyLong(), any(UpdateProductVariantRequest.class)))
                .thenThrow(new NotFoundException(getMessage(PRODUCT_VARIANT_NOT_FOUND)));

        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(PRODUCT_VARIANT_NOT_FOUND), ID_PATH);
    }

    @Test
    @DisplayName("상품 변형 수정 테스트-실패(옵션 값 아이디가 상품에 지정된 옵션 타입의 옵션 값과 다를때)")
    void updateProductVariantTest_notMatchType() throws Exception {
        UpdateProductVariantRequest request = createUpdateProductVariantRequest();

        when(service.updateVariantById(anyLong(), any(UpdateProductVariantRequest.class)))
                .thenThrow(new BadRequestException(getMessage(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE)));

        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                getMessage(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE), ID_PATH);
    }

    @Test
    @DisplayName("상품 변형 수정 테스트-실패(상품 옵션 타입의 수와 옵션 값의 개수가 맞지 않을때)")
    void updateProductVariantTest_cardinality_violation() throws Exception {
        UpdateProductVariantRequest request = createUpdateProductVariantRequest();
        when(service.updateVariantById(anyLong(), any(UpdateProductVariantRequest.class)))
                .thenThrow(new BadRequestException(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION)));

        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION), ID_PATH);
    }

    private UpdateProductVariantRequest createUpdateProductVariantRequest(){
        return new UpdateProductVariantRequest(3000, 100, 10, List.of(1L, 2L));
    }
}

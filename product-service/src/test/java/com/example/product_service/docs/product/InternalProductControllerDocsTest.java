package com.example.product_service.docs.product;

import com.example.product_service.api.product.controller.InternalProductController;
import com.example.product_service.api.product.controller.dto.InternalVariantRequest;
import com.example.product_service.api.product.service.VariantService;
import com.example.product_service.api.product.service.dto.result.InternalVariantResponse;
import com.example.product_service.docs.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InternalProductControllerDocsTest extends RestDocsSupport {
    private VariantService variantService = Mockito.mock(VariantService.class);
    @Override
    protected Object initController() {
        return new InternalProductController(variantService);
    }

    @Test
    @DisplayName("상품 변형을 조회한다")
    void getVariant() throws Exception {
        //given
        InternalVariantResponse response = createVariantResponse();
        given(variantService.getVariant(anyLong())).willReturn(response);
        //when
        //then
        mockMvc.perform(get("/internal/variants/{variantId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("internal-get-variant",
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("variantId").description("조회할 상품 변형 ID")
                                ),
                                responseFields(
                                        fieldWithPath("productId").description("상품 Id"),
                                        fieldWithPath("productVariantId").description("상품 변형 Id"),
                                        fieldWithPath("sku").description("상품 SKU"),
                                        fieldWithPath("productName").description("상품 이름"),
                                        fieldWithPath("unitPrice.originalPrice").description("상품 변형 원본 가격"),
                                        fieldWithPath("unitPrice.discountRate").description("상품 변형 할인율"),
                                        fieldWithPath("unitPrice.discountAmount").description("상품 변형 할인 금액"),
                                        fieldWithPath("unitPrice.discountedPrice").description("상품 변형 할인된 금액"),
                                        fieldWithPath("stockQuantity").description("상품 변형 재고 수량"),
                                        fieldWithPath("thumbnailUrl").description("상품 썸네일"),
                                        fieldWithPath("itemOptions[].optionTypeName").description("상품 변형 옵션 타입"),
                                        fieldWithPath("itemOptions[].optionValueName").description("상품 변형 옵션 값")
                                )
                        )
                );
    }

    @Test
    @DisplayName("상품 변형 리스트를 조회한다")
    void getVariants() throws Exception {
        //given
        List<InternalVariantResponse> response = List.of(createVariantResponse());
        InternalVariantRequest request = InternalVariantRequest.builder().variantIds(List.of(1L)).build();
        given(variantService.getVariants(anyList())).willReturn(response);
        //when
        //then
        mockMvc.perform(post("/internal/variants/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("internal-get-variants",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("variantIds").description("조회할 상품 변형 Id 리스트").optional()
                                ),
                                responseFields(
                                        fieldWithPath("[].productId").description("상품 Id"),
                                        fieldWithPath("[].productVariantId").description("상품 변형 Id"),
                                        fieldWithPath("[].sku").description("상품 SKU"),
                                        fieldWithPath("[].productName").description("상품 이름"),
                                        fieldWithPath("[].unitPrice.originalPrice").description("상품 변형 원본 가격"),
                                        fieldWithPath("[].unitPrice.discountRate").description("상품 변형 할인율"),
                                        fieldWithPath("[].unitPrice.discountAmount").description("상품 변형 할인 금액"),
                                        fieldWithPath("[].unitPrice.discountedPrice").description("상품 변형 할인된 금액"),
                                        fieldWithPath("[].stockQuantity").description("상품 변형 재고 수량"),
                                        fieldWithPath("[].thumbnailUrl").description("상품 썸네일"),
                                        fieldWithPath("[].itemOptions[].optionTypeName").description("상품 변형 옵션 타입"),
                                        fieldWithPath("[].itemOptions[].optionValueName").description("상품 변형 옵션 값")
                                )
                        )
                );
    }

    private InternalVariantResponse createVariantResponse() {
        return InternalVariantResponse.builder()
                .productId(1L)
                .productVariantId(1L)
                .productName("상품")
                .sku("TEST")
                .unitPrice(
                        InternalVariantResponse.UnitPrice.builder()
                                .originalPrice(3000L)
                                .discountRate(10)
                                .discountAmount(300L)
                                .discountedPrice(2700L)
                                .build())
                .stockQuantity(100)
                .thumbnailUrl("http://thumbnail.jpg")
                .itemOptions(
                        List.of(
                                InternalVariantResponse.ItemOption.builder()
                                        .optionTypeName("사이즈")
                                        .optionValueName("XL")
                                        .build()
                        )
                ).build();
    }
}

package com.example.product_service.api.product.controller;

import com.example.product_service.api.product.controller.dto.InternalVariantRequest;
import com.example.product_service.api.product.service.dto.result.InternalVariantResponse;
import com.example.product_service.support.ControllerTestSupport;
import com.example.product_service.support.security.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
public class InternalProductControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("상품 변형을 조회한다")
    void getVariant() throws Exception {
        //given
        InternalVariantResponse response = createVariantResponse();
        given(variantService.getVariant(anyLong()))
                        .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/internal/variants/{variantId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("상품 변형 목록을 조회한다")
    void getVariants() throws Exception {
        //given
        InternalVariantRequest request = InternalVariantRequest.builder().variantIds(List.of(1L)).build();
        List<InternalVariantResponse> response = List.of(createVariantResponse());
        given(variantService.getVariants(anyList()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/internal/variants/by-ids")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("상품 변형 아이디가 비어있는 경우 예외를 던진다")
    void getVariants_empty_ids() throws Exception {
        //given
        InternalVariantRequest request = InternalVariantRequest.builder().variantIds(List.of()).build();
        //when
        //then
        mockMvc.perform(post("/internal/variants/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("아이디는 필수 입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/internal/variants/by-ids"));
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
